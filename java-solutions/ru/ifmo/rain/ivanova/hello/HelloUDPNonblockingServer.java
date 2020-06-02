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
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPNonblockingServer implements HelloServer {
    private Selector selector;
    private DatagramChannel datagramChannel;
    private int bufferSize;
    private ExecutorService executorService;
    private ExecutorService worker;
    private SelectionKey key;

    private byte[] bytes;

    private class PairBuffer {
        ByteBuffer data;
        SocketAddress socketAddress;

        PairBuffer(final ByteBuffer data, final SocketAddress socketAddress) {
            this.data = data;
            this.socketAddress = socketAddress;
        }
    }

    private final Queue<PairBuffer> empty = new ArrayDeque<>();
    private final Queue<PairBuffer> fill = new ArrayDeque<>();

    private void fillEmpty(final int threads) {
        for (int i = 0; i < threads; i++) {
            empty.add(new PairBuffer(ByteBuffer.allocate(bufferSize), new InetSocketAddress(0)));
        }
    }

    private PairBuffer getEmpty() {
        synchronized (empty) {
            PairBuffer buffer = empty.remove();
            if (empty.isEmpty()) {
                HelloUDPUtills.changeInterestFromRead(key, selector);
            }
            return buffer;
        }
    }

    private PairBuffer getFill() {
        synchronized (fill) {
            PairBuffer buffer = fill.remove();
            if (fill.isEmpty()) {
                HelloUDPUtills.changeInterestFromWrite(key, selector);
            }
            return buffer;
        }
    }

    private void read(final PairBuffer buffer, final SocketAddress socketAddress) {
        byte[] bytes = HelloUDPUtills.getBytes("Hello, " + StandardCharsets.UTF_8.decode(buffer.data.flip()).toString());
        buffer.data.clear().put(bytes).flip();
        buffer.socketAddress = socketAddress;
        synchronized (fill) {
            if (fill.size() == 0) {
                HelloUDPUtills.changeInterestToWrite(key, selector);
            }
            fill.add(buffer);
        }
    }

    private void write(final PairBuffer buffer) {
        buffer.data.clear().flip();
        synchronized (empty) {
            if (empty.isEmpty()) {
                HelloUDPUtills.changeInterestToRead(key, selector);
            }
            empty.add(buffer);
        }
    }

    private void readServer() throws IOException {
        final PairBuffer buffer = getEmpty();
        SocketAddress socketAddress = datagramChannel.receive(buffer.data.clear());
        executorService.submit(() -> read(buffer, socketAddress));
    }

    private void writeServer() throws IOException {
        final PairBuffer pairBuffer = getFill();
        datagramChannel.send(pairBuffer.data, pairBuffer.socketAddress);
        write(pairBuffer);
    }

    private void run() {
        while (!Thread.interrupted() && !datagramChannel.socket().isClosed()) {
            try {
                selector.select();
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                    key = i.next();
                    try {
                        if (key.isReadable()) {
                            readServer();
                        }
                        if (key.isWritable()) {
                            writeServer();
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

    private boolean selectorOpen = false;
    private boolean channelOpen = false;

    @Override
    public void start(final int port, final int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        worker = Executors.newSingleThreadExecutor();
        try {
            selector = Selector.open();
            selectorOpen = true;
            datagramChannel = DatagramChannel.open();
            channelOpen = true;
            datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(port));
            bufferSize = datagramChannel.socket().getReceiveBufferSize();
            datagramChannel.register(selector, SelectionKey.OP_READ);
            bytes = new byte[bufferSize + 10];
            fillEmpty(threads);
            worker.submit(this::run);
        } catch (IOException e) {
            System.out.println("Can't open channel " + e);
        }
    }

    @Override
    public void close() {
        try {
            if (selectorOpen) {
                selector.close();
            }
            if (channelOpen) {
                datagramChannel.close();
            }
            HelloUDPUtills.closeExecutorService(worker);
            HelloUDPUtills.closeExecutorService(executorService);
        } catch (IOException e) {
            System.out.println("Can't close " + e);
        }
    }

    public static void main(final String[] args) {
        try (final HelloUDPNonblockingServer server = new HelloUDPNonblockingServer()) {
            HelloUDPUtills.main(args, server);
        }
    }
}