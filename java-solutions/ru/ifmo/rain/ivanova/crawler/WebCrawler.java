package ru.ifmo.rain.ivanova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * @author sasha.pff
 * <p>
 * Implementation of Crawler for websites.
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;
    private final static int TIMEOUT = 1000;

    /**
     * Constructor to initialize with {@code Downloader}, number of downloading pages,
     * number of processing pages and number of downloading pages per one host.
     *
     * @param downloader  {@code Downloader} to use.
     * @param downloaders number of downloading pages.
     * @param extractors  number of processing pages.
     * @param perHost     number of downloading pages per one host.
     */
    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    private class HostDownloader {
        private final Queue<Runnable> tasks = new ArrayDeque<>();
        private int readyToRun = perHost;

        synchronized private void finish() {
            readyToRun++;
        }

        synchronized private void ready() {
            if (readyToRun == 0) {
                return;
            }
            run();
        }

        private void run() {
            final Runnable task = tasks.poll();
            if (task != null) {
                readyToRun--;
                downloaders.submit(() -> {
                    try {
                        task.run();
                    } catch (final Exception ignored) {
                    } finally {
                        finish();
                        ready();
                    }
                });
            }
        }

        synchronized void submit(final Runnable task) {
            tasks.add(task);
            ready();
        }
    }

    private class WebDownloader {
        private final List<String> downloaded = Collections.synchronizedList(new ArrayList<>());
        private final ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        private final Set<String> addedUrls = ConcurrentHashMap.newKeySet();
        private final ConcurrentMap<String, HostDownloader> addedHosts = new ConcurrentHashMap<>();
        private Phaser phaser;
        private ConcurrentLinkedQueue<String> queueToTake = new ConcurrentLinkedQueue<>();
        private ConcurrentLinkedQueue<String> queueToAdd = new ConcurrentLinkedQueue<>();

        private String getHost(final String url) {
            try {
                return URLUtils.getHost(url);
            } catch (final MalformedURLException e) {
                errors.put(url, e);
                return null;
            }
        }

        private void download(final String url, final int d) {
            final String host = getHost(url);
            if (host == null) {
                return;
            }

            phaser.register();
            addedHosts.computeIfAbsent(host, unused -> new HostDownloader()).submit(() -> {
                try {
                    final Document document = downloader.download(url);
                    if (d > 1) {
                        phaser.register();
                        extractors.submit(() -> {
                            try {
                                queueToAdd.addAll(document.extractLinks());
                            } catch (final IOException e) {
                                errors.put(url, e);
                            } finally {
                                phaser.arrive();
                            }
                        });
                    }
                    downloaded.add(url);
                } catch (final IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arrive();
                }
            });
        }

        void swapQueues() {
            final ConcurrentLinkedQueue<String> tmp = queueToAdd;
            queueToAdd = queueToTake;
            queueToTake = tmp;
        }

        Result run(final String url, final int depth) {
            queueToAdd.add(url);
            IntStream.range(0, depth).forEachOrdered(d -> {
                swapQueues();
                phaser = new Phaser();
                phaser.register();
                queueToTake.stream()
                        .filter(addedUrls::add)
                        .forEach(url1 -> download(url1, depth - d));
                queueToTake.clear();
                phaser.arriveAndAwaitAdvance();
            });
            return new Result(downloaded, errors);
        }
    }

    @Override
    public Result download(final String url, final int depth) {
        return new WebDownloader().run(url, depth);
    }

    @Override
    public void close() {
        extractors.shutdown();
        downloaders.shutdown();
        try {
            extractors.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
            downloaders.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            System.err.println("Can't terminate");
        }
    }

    private static int parseArgument(final String[] args, final int index) {
        return args.length <= index ? 1 : Integer.parseInt(args[index]);
    }

    /**
     * Main function to run using command line.
     *
     * @param args arguments to create {@code WebCrawler}.
     */
    public static void main(final String[] args) {
        if (args == null || args.length == 0 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Incorrect arguments");
            return;
        }
        final int depth = parseArgument(args, 1);
        try (final Crawler crawler = new WebCrawler(new CachingDownloader(), depth,
                parseArgument(args, 2), parseArgument(args, 3))) {
            crawler.download(args[0], depth);
        } catch (final IOException e) {
            System.err.println("Can't initialize CachingDownloader");
        }
    }
}
