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

    private void task(String host, int port, String prefix, int thread, int requests) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
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
                            packet.setData(new byte[receiveBufferSize]);
                            datagramSocket.receive(packet);
                            responseMessage = HelloUDPUtills.getString(packet);
                            success = responseMessage.matches(
                                    "[\\D]*" + Integer.toString(thread) +
                                            "[\\D]*" + Integer.toString(i) + "[\\D]*");
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
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
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
