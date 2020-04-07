package ru.ifmo.rain.ivanova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

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
    private final ConcurrentMap<String, HostTasks> addedHosts;

    /**
     * Constructor to initialize with {@code Downloader}, number of downloading pages,
     * number of processing pages and number of downloading pages per one host.
     *
     * @param downloader  {@code Downloader} to use.
     * @param downloaders number of downloading pages.
     * @param extractors  number of processing pages.
     * @param perHost     number of downloading pages per one host.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        addedHosts = new ConcurrentHashMap<>();
    }

    private class HostTasks {
        private final Queue<Runnable> tasks = new ArrayDeque<>();
        private int running = 0;

        synchronized void runTask() {
            if (running < perHost) {
                Runnable task = tasks.poll();
                if (task != null) {
                    running++;
                    downloaders.submit(() -> {
                        try {
                            task.run();
                        } finally {
                            running--;
                            runTask();
                        }
                    });
                }
            }
        }

        synchronized void addTask(Runnable task) {
            tasks.add(task);
            runTask();
        }

    }

    private class Worker {
        private final Set<String> successUrls = ConcurrentHashMap.newKeySet();
        private final ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        private final Set<String> addedUrls = ConcurrentHashMap.newKeySet();

        private void download(String url, Phaser phaser, int height) {
            String host;
            try {
                host = URLUtils.getHost(url);
            } catch (MalformedURLException e) {
                errors.put(url, e);
                return;
            }
            phaser.register();
            addedHosts.computeIfAbsent(host, unused -> new HostTasks()).addTask(() -> {
                try {
                    Document document = downloader.download(url);
                    successUrls.add(url);
                    if (height > 1) {
                        phaser.register();
                        extractors.submit(() -> {
                            try {
                                queue.addAll(document.extractLinks());
                            } catch (IOException ignored) {
                            } finally {
                                phaser.arrive();
                            }
                        });
                    }
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arrive();
                }
            });
        }

        private void run(final int depth) {
            for (int d = 0; d < depth; d++) {
                int index = d;
                final Phaser phaser = new Phaser(1);
                List<String> currentQueue = new ArrayList<>(queue);
                queue.clear();
                currentQueue.stream()
                        .filter(addedUrls::add)
                        .forEach(url -> download(url, phaser, depth - index));
                phaser.arriveAndAwaitAdvance();
            }
        }

        Worker(String url, int depth) {
            queue.add(url);
            run(depth);
        }

        Result result() {
            return new Result(new ArrayList<>(successUrls), errors);
        }
    }

    @Override
    public Result download(String url, int depth) {
        return new Worker(url, depth).result();
    }

    @Override
    public void close() {
        extractors.shutdown();
        downloaders.shutdown();
        try {
            extractors.awaitTermination(0, TimeUnit.SECONDS);
            downloaders.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Can't terminate");
        }
    }

    private static int parseArgument(String[] args, int index) {
        return args.length <= index ? 1 : Integer.parseInt(args[index]);
    }

    /**
     * Main function to run using command line.
     *
     * @param args arguments to create {@code WebCrawler}.
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || Arrays.stream(args).anyMatch(Objects::nonNull)) {
            System.err.println("Incorrect arguments");
            return;
        }
        int depth = parseArgument(args, 1);
        try (Crawler crawler = new WebCrawler(new CachingDownloader(), depth,
                parseArgument(args, 2), parseArgument(args, 3))) {
            crawler.download(args[0], depth);
        } catch (IOException e) {
            System.err.println("Can't initialize CachingDownloader");
        }
    }
}