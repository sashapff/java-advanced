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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class HelloUDPNonblockingClient implements HelloClient {
    private Integer counter = 0;

    private class Context {
        ByteBuffer buffer;
        int thread;
        int request = 0;

        Context(final ByteBuffer buffer, final int thread) {
            this.buffer = buffer;
            this.thread = thread;
        }

        void increase() {
            request++;
        }
    }

    private void read(final SelectionKey key, final int requests) throws IOException {
        final DatagramChannel channel = (DatagramChannel) key.channel();
        final Context context = (Context) key.attachment();
        context.buffer.clear();
        channel.receive(context.buffer);
        context.buffer.flip();
        String message = StandardCharsets.UTF_8.decode(context.buffer).toString();
//        System.out.println("mes   " + message);
        if (HelloUDPUtills.checkInts(message, context.thread, context.request)) {
            context.increase();
            if (context.request != requests) {
                key.interestOps(SelectionKey.OP_WRITE);
            } else {
                synchronized (counter) {
                    key.channel().close();
                    counter++;
                }
            }
//            System.out.println("good" + context.request + " " + requests);
        } else {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void write(final SelectionKey key, final String prefix) throws IOException {
        final DatagramChannel channel = (DatagramChannel) key.channel();
        final Context context = (Context) key.attachment();
        final String request = prefix + context.thread + "_" + context.request;
//        System.out.println("req   " + request);
        context.buffer.clear().put(request.getBytes(StandardCharsets.UTF_8));
        channel.send(context.buffer.flip(), channel.getRemoteAddress());
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
//        try (final Selector selector = Selector.open()) {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.out.println("Can't run");
        }
//            System.out.println("threads" + threads);
        for (int i = 0; i < threads; i++) {
            try {
                final DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                datagramChannel.configureBlocking(false);
                datagramChannel.connect(new InetSocketAddress(InetAddress.getByName(host), port));
                final int bufferSize = datagramChannel.socket().getReceiveBufferSize();
                datagramChannel.register(selector, SelectionKey.OP_WRITE,
                        new Context(ByteBuffer.allocate(bufferSize), i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        while (!Thread.interrupted() && counter != threads) {
            try {
                selector.select(200);
                if (selector.selectedKeys().isEmpty()) {
                    for (SelectionKey key : selector.keys()) {
                        if (key.isWritable()) {
                            write(key, prefix);
                        }
                    }
                } else {
                    for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                        final SelectionKey key = i.next();
                        try {
                            if (key.isWritable()) {
                                write(key, prefix);
                            }
                            if (key.isReadable()) {
                                read(key, requests);
                            }
                        } finally {
                            i.remove();
                        }
                    }
                }
            } catch (IOException e) {

            }
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Incorrect arguments");
            return;
        }
        new HelloUDPNonblockingClient().run(args[0], Integer.parseInt(args[1]), args[2],
                Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }
}