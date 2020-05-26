package ru.ifmo.rain.ivanova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPNonblockingServer implements HelloServer {
    private Selector selector;
    private DatagramChannel datagramChannel;
    private int bufferSize;
    private ExecutorService executorService;
    private ExecutorService worker;

    private class PairBuffer {
        ByteBuffer data;
        SocketAddress socketAddress;

        PairBuffer(final ByteBuffer data, final SocketAddress socketAddress) {
            this.data = data;
            this.socketAddress = socketAddress;
        }
    }

    private final Queue<ByteBuffer> empty = new ArrayDeque<>();
    private final Queue<PairBuffer> fill = new ArrayDeque<>();

    private void fillEmpty(final int threads) {
        for (int i = 0; i < threads; i++) {
            empty.add(ByteBuffer.allocate(bufferSize));
        }
    }

    private ByteBuffer getEmpty(final SelectionKey key) {
        synchronized (empty) {
            if (empty.size() == 1) {
                HelloUDPUtills.changeInterestFromRead(key, selector);
            }
            return empty.remove();
        }
    }

    private PairBuffer getFill(final SelectionKey key) {
        synchronized (fill) {
            if (fill.size() == 1) {
                HelloUDPUtills.changeInterestFromWrite(key, selector);
            }
            return fill.remove();
        }
    }

    private void read(final ByteBuffer buffer, final SocketAddress socketAddress, final SelectionKey key) {
        String message = "Hello, " + StandardCharsets.UTF_8.decode(buffer.flip()).toString();
        final byte[] bytes = HelloUDPUtills.getBytes(message);
        synchronized (fill) {
            if (fill.isEmpty()) {
                HelloUDPUtills.changeInterestToWrite(key, selector);
            }
            fill.add(new PairBuffer(buffer.clear().put(bytes).flip(), socketAddress));
        }
    }

    private synchronized void write(final ByteBuffer buffer, final SelectionKey key) {
        synchronized (empty) {
            if (empty.isEmpty()) {
                HelloUDPUtills.changeInterestToRead(key, selector);
            }
            empty.add(buffer.clear().flip());
        }
    }

    private void readServer(final SelectionKey key) throws IOException {
        final ByteBuffer buffer = getEmpty(key);
        SocketAddress socketAddress = datagramChannel.receive(buffer.clear());
        executorService.submit(() -> read(buffer, socketAddress, key));
    }

    private void writeServer(final SelectionKey key) throws IOException {
        final PairBuffer pairBuffer = getFill(key);
        datagramChannel.send(pairBuffer.data, pairBuffer.socketAddress);
        executorService.submit(() -> write(pairBuffer.data, key));
    }

    private void run() {
        while (!Thread.interrupted() && !datagramChannel.socket().isClosed()) {
            try {
                selector.select();
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                    final SelectionKey key = i.next();
                    try {
                        if (key.isReadable()) {
                            readServer(key);
                        }
                        if (key.isWritable()) {
                            writeServer(key);
                        }
                    } finally {
                        i.remove();
                    }
                }
            } catch (IOException e) {
                System.out.println("Can't select");
            }
        }
    }

    @Override
    public void start(final int port, final int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        worker = Executors.newSingleThreadExecutor();
        try {
            selector = Selector.open();
            datagramChannel = DatagramChannel.open();
            datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(port));
            bufferSize = datagramChannel.socket().getReceiveBufferSize();
            datagramChannel.register(selector, SelectionKey.OP_READ);
            fillEmpty(threads);
            worker.submit(this::run);
        } catch (IOException e) {
            System.out.println("Can't start " + e);
        }
    }

    @Override
    public void close() {
        try {
            if (selector.isOpen()) {
                selector.close();
            }
            if (datagramChannel.isOpen()) {
                datagramChannel.close();
            }
            worker.shutdown();
            executorService.shutdown();
            try {
                worker.awaitTermination(100, TimeUnit.SECONDS);
                executorService.awaitTermination(100, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                System.out.println("Can't terminate ExecutorService " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("Can't close " + e);
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Incorrect arguments");
            return;
        }
        try (final HelloUDPNonblockingServer server = new HelloUDPNonblockingServer()) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            System.out.println("Enter something to close server");
            new Scanner(System.in).next();
        } catch (Exception ignored) {
        }
    }
}