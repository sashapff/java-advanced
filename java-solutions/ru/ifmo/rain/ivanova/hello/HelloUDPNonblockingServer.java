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
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPNonblockingServer implements HelloServer {
    private Selector selector;
    private DatagramChannel datagramChannel;
    private int bufferSize;
    private ExecutorService executorService;
    private ExecutorService worker;
    private SelectionKey key;

    private class PairBuffer {
        ByteBuffer data;
        SocketAddress socketAddress;

        PairBuffer(final ByteBuffer data, final SocketAddress socketAddress) {
            this.data = data;
            this.socketAddress = socketAddress;
        }
    }

    private final ConcurrentLinkedQueue<PairBuffer> empty = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<PairBuffer> fill = new ConcurrentLinkedQueue<>();

    private void fillEmpty(final int threads) {
        for (int i = 0; i < threads; i++) {
            empty.add(new PairBuffer(ByteBuffer.allocate(bufferSize), new InetSocketAddress(0)));
        }
    }

    private PairBuffer getEmpty() {
        PairBuffer buffer = empty.remove();
        if (empty.isEmpty()) {
            HelloUDPUtills.changeInterestFromRead(key, selector);
        }
        return buffer;
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
        byte[] bytes = HelloUDPUtills.getBytes("Hello, " + HelloUDPUtills.decode(buffer.data.flip()));
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
        if (empty.isEmpty()) {
            HelloUDPUtills.changeInterestToRead(key, selector);
        }
        empty.add(buffer);
    }

    private void readServer() throws IOException {
        final PairBuffer buffer = getEmpty();
        SocketAddress socketAddress = datagramChannel.receive(buffer.data.clear());
        executorService.submit(() -> read(buffer, socketAddress));
    }

    private void writeServer() throws IOException {
        final PairBuffer buffer = getFill();
        datagramChannel.send(buffer.data, buffer.socketAddress);
        write(buffer);
    }

    private void run() {
        while (!Thread.interrupted() && !datagramChannel.socket().isClosed() && selector.isOpen()) {
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
                return;
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
            System.out.println("Can't open channel " + e);
        }
    }

    @Override
    public void close() {
        try {
            if (selector != null) {
                selector.close();
            }
            if (datagramChannel != null) {
                datagramChannel.close();
            }
            HelloUDPUtills.closeExecutorService(worker);
            HelloUDPUtills.closeExecutorService(executorService);
        } catch (IOException e) {
            System.out.println("Can't close " + e);
        }
    }

    public static void main(final String[] args) {
        HelloUDPUtills.mainServer(args, new HelloUDPNonblockingServer());
    }
}