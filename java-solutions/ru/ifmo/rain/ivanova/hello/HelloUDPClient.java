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
    class Pair {
        boolean result;
        int i;

        Pair(final boolean result, final int i) {
            this.result = result;
            this.i = i;
        }
    }

    private int skipChars(final String s, int i) {
        while (i < s.length() && !Character.isDigit(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private boolean checkSubStr(final String s, final String as, final int i) {
        return !s.substring(i, i + as.length()).equals(as);
    }

    private Pair checkInt(int i, final String s, final String as) {
        i = skipChars(s, i);
        if (i == s.length() || checkSubStr(s, as, i)) {
            return new Pair(false, i);
        }
        i += as.length();
        return new Pair(true, i);
    }

    private boolean checkInts(final String s, final int a, final int b) {
        int i = 0;
        final Pair ap = checkInt(i, s, Integer.toString(a));
        final Pair bp = checkInt(ap.i, s, Integer.toString(b));
        if (!bp.result || !ap.result) {
            return false;
        }
        return skipChars(s, bp.i) == s.length();
    }


    private void task(final String host, final int port, final String prefix, final int thread, final int requests) {
        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(100);
            final int bufferSize = datagramSocket.getSendBufferSize();
            byte[] request;
            byte[] response = new byte[bufferSize];
            String message;
            try {
                final DatagramPacket packet
                        = HelloUDPUtills.newDatagramPacket(response, host, port);
                for (int i = 0; i < requests; i++) {
                    message = prefix + thread + "_" + i;
                    request = HelloUDPUtills.getBytes(message);
                    System.err.println(message);
                    boolean success = false;
                    while (!success && !datagramSocket.isClosed()) {
                        packet.setData(request);
                        try {
                            message = HelloUDPUtills.sendAndReceive(datagramSocket, packet, response);
                            success = checkInts(message, thread, i);
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
            executorService.awaitTermination(10 * threads * requests, TimeUnit.SECONDS);
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
