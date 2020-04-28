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

        Pair(boolean result, int i) {
            this.result = result;
            this.i = i;
        }
    }

    private Pair check(int i, String s, String as) {
        i = skipChars(s, i);
        if (i == s.length()) {
            return new Pair(false, i);
        }
        if (checkSubStr(s, as, i)) {
            return new Pair(false, i);
        }
        i += as.length();
        return new Pair(true, i);
    }

    private int skipChars(String s, int i) {
        while (i < s.length() && !Character.isDigit(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private boolean checkSubStr(String s, String as, int i) {
        return !s.substring(i, i + as.length()).equals(as);
    }

    private boolean check(String s, int a, int b) {
        int i = 0;
        Pair ap = check(i, s, Integer.toString(a));
        if (!ap.result) {
            return false;
        }
        i = ap.i;
        Pair bp = check(i, s, Integer.toString(b));
        if (!bp.result) {
            return false;
        }
        i = bp.i;
        return skipChars(s, i) == s.length();
    }


    private void task(String host, int port, String prefix, int thread, int requests) {
        try (final DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(100);
            int receiveBufferSize = datagramSocket.getReceiveBufferSize();
            try {
                DatagramPacket packet
                        = HelloUDPUtills.newDatagramPacket(receiveBufferSize, host, port);
                for (int i = 0; i < requests; i++) {
                    String requestMessage = prefix + Integer.toString(thread) + "_" + Integer.toString(i);
                    byte[] request = HelloUDPUtills.getBytes(requestMessage);
                    String responseMessage = "";
                    System.err.println(requestMessage);
                    boolean success = false;
                    while (!success && !datagramSocket.isClosed()) {
                        packet.setData(request, 0, request.length);
                        try {
                            datagramSocket.send(packet);
                            packet.setData(new byte[receiveBufferSize], 0, receiveBufferSize);
                            datagramSocket.receive(packet);
                            responseMessage = HelloUDPUtills.getString(packet);
                            success = check(responseMessage, thread, i);
                        } catch (IOException e) {
                            System.out.println("Cant't send DatagramPacket " + e.getMessage());
                        }
                    }
                    System.err.println(responseMessage);
                }
            } catch (UnknownHostException e) {
                System.out.println("Can't get host name " + e.getMessage());
            }
        } catch (SocketException e) {
            System.out.println("Can't create DatagramSocket " + e.getMessage());
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            int index = i;
            executorService.submit(() -> task(host, port, prefix, index, requests));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(10 * threads * requests, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Can't terminate ExecutorService " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Incorrect arguments");
            return;
        }
        new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }
}
