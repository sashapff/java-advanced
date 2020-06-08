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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HelloUDPNonblockingClient implements HelloClient {
    private String prefix;
    private int requests;
    private List<DatagramChannel> datagramChannelList = new ArrayList<>();

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
            channel.receive(buffer.clear());
            if (HelloUDPUtills.checkInts(HelloUDPUtills.decode(buffer.flip()), thread, request)) {
                increaseRequests();
                if (request == requests) {
                    channel.close();
                    return;
                }
            }
            key.interestOps(SelectionKey.OP_WRITE);
        }

        void write(final SelectionKey key) throws IOException {
            final DatagramChannel channel = (DatagramChannel) key.channel();
            channel.send(buffer.clear().put((prefix + thread + "_" + request).
                    getBytes(StandardCharsets.UTF_8)).flip(), channel.getRemoteAddress());
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void readClient(final SelectionKey key) throws IOException {
        ((Context) key.attachment()).read(key);

    }

    private void writeClient(final SelectionKey key) throws IOException {
        ((Context) key.attachment()).write(key);
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        System.out.println("START PORT " + port);
        this.prefix = prefix;
        this.requests = requests;
        try (final Selector selector = Selector.open()) {
            for (int i = 0; i < threads; i++) {
                try {
                    final DatagramChannel datagramChannel = DatagramChannel.open();
                    datagramChannelList.add(datagramChannel);
                    datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    datagramChannel.configureBlocking(false);
                    datagramChannel.connect(new InetSocketAddress(InetAddress.getByName(host), port));
                    datagramChannel.register(selector, SelectionKey.OP_WRITE,
                            new Context(ByteBuffer.allocate(datagramChannel.socket().getReceiveBufferSize()), i));
                } catch (IOException e) {
                    System.out.println("Can't open DatagramChannel");
                }
            }
            while (!Thread.interrupted() && selector.isOpen() && !selector.keys().isEmpty()) {
                try {
                    selector.select(10);
                    if (!selector.selectedKeys().isEmpty()) {
                        for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                            final SelectionKey key = i.next();
                            try {
                                if (key.isWritable()) {
                                    writeClient(key);
                                }
                                if (key.isReadable()) {
                                    readClient(key);
                                }
                            } finally {
                                i.remove();
                            }
                        }
                    } else {
                        for (final SelectionKey key : selector.keys()) {
                            if (key.isWritable()) {
                                writeClient(key);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Can't select");
                }
            }
            System.out.println("Finish while");
        } catch (IOException e) {
            System.out.println("Can't open Selector");
            return;
        }

        for (DatagramChannel datagramChannel : datagramChannelList) {
            if (datagramChannel != null) {
                try {
                    datagramChannel.close();
                } catch (IOException e) {
                    System.out.println("Can't close datagram channel");
                }
            }
        }
    }

    public static void main(final String[] args) {
        HelloUDPUtills.mainClient(args, new HelloUDPNonblockingClient());
    }
}
