package ru.ifmo.rain.ivanova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class HelloUDPNonblockingClient implements HelloClient {
    private String prefix;
    private int requests;
    private byte[][] PREFIX;

    private class Context {
        ByteBuffer buffer;
        private int thread;
        private int request = 0;

        Context(final ByteBuffer buffer, final int thread) {
            this.buffer = buffer;
            this.thread = thread;
        }

        void increaseRequests() {
            request++;
        }

        void read(final SelectionKey key) throws IOException {
            final DatagramChannel channel = (DatagramChannel) key.channel();
            channel.receive(buffer);
            buffer.flip();
            if (HelloUDPUtills.checkInts(HelloUDPUtills.decode(buffer), thread, request)) {
                increaseRequests();
                if (request == requests) {
                    channel.close();
                    return;
                }
            }
            buffer.clear();
            key.interestOps(SelectionKey.OP_WRITE);
        }

        void write(final SelectionKey key) throws IOException {
            final DatagramChannel channel = (DatagramChannel) key.channel();
            buffer.put(PREFIX[thread]);
            buffer.put(Integer.toString(request).getBytes()).flip();
            channel.send(buffer, channel.getRemoteAddress());
            buffer.clear();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void run(final Selector selector) {
        while (!Thread.interrupted() && selector.isOpen() && !selector.keys().isEmpty()) {
            try {
                selector.select(10);
                if (!selector.selectedKeys().isEmpty()) {
                    for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                        final SelectionKey key = i.next();
                        try {
                            if (key.isWritable()) {
                                ((Context) key.attachment()).write(key);
                            }
                            if (key.isReadable()) {
                                ((Context) key.attachment()).read(key);
                            }
                        } finally {
                            i.remove();
                        }
                    }
                } else {
                    for (final SelectionKey key : selector.keys()) {
                        if (key.isWritable()) {
                            ((Context) key.attachment()).write(key);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Can't select");
            }
        }
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        this.prefix = prefix;
        this.requests = requests;
        try (final Selector selector = Selector.open()) {
            PREFIX = new byte[requests][100];
            for (int i = 0; i < threads; i++) {
                try {
                    final DatagramChannel datagramChannel = DatagramChannel.open();
                    datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    datagramChannel.configureBlocking(false);
                    datagramChannel.connect(new InetSocketAddress(InetAddress.getByName(host), port));
                    datagramChannel.register(selector, SelectionKey.OP_WRITE,
                            new Context(ByteBuffer.allocate(datagramChannel.socket().getReceiveBufferSize()), i));
                    PREFIX[i] = HelloUDPUtills.getBytes(prefix + i + "_");
                } catch (IOException e) {
                    System.out.println("Can't open DatagramChannel");
                    return;
                }
            }
            run(selector);
        } catch (IOException e) {
            System.out.println("Can't open Selector");
        }
    }

    public static void main(final String[] args) {
        HelloUDPUtills.mainClient(args, new HelloUDPNonblockingClient());
    }
}
