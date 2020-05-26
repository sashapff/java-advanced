package ru.ifmo.rain.ivanova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    private void task(final String host, final int port, final String prefix, final int thread, final int requests) {
        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(100);
            final int bufferSize = datagramSocket.getSendBufferSize();
            final byte[] response = new byte[bufferSize];
            try {
                final DatagramPacket packet
                        = HelloUDPUtills.newDatagramPacket(response, host, port);
                for (int i = 0; i < requests; i++) {
                    String message = prefix + thread + "_" + i;
                    final byte[] request = HelloUDPUtills.getBytes(message);
                    System.err.println(message);
                    boolean success = false;
                    while (!success && !datagramSocket.isClosed()) {
                        try {
                            HelloUDPUtills.send(datagramSocket, packet, request);
                            message = HelloUDPUtills.receive(datagramSocket, packet, response);
                            success = HelloUDPUtills.checkInts(message, thread, i);
                        } catch (final IOException e) {
                            System.out.println("Cant't send DatagramPacket " + e.getMessage());
                        }
                    }
                    System.err.println(message);
                }
            } catch (final UnknownHostException e) {
                System.out.println("Can't get host name " + e.getMessage());
            }
        } catch (final SocketException e) {
            System.out.println("Can't create DatagramSocket " + e.getMessage());
        }
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int index = i;
            executorService.submit(() -> task(host, port, prefix, index, requests));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(10 * requests, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            System.out.println("Can't terminate ExecutorService " + e.getMessage());
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Incorrect arguments");
            return;
        }
        new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }
}
