package ru.ifmo.rain.ivanova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket datagramSocket;
    private ExecutorService executorService;
    private int receiveBufferSize;

    private void task() {
        final DatagramPacket packet = HelloUDPUtills.newDatagramPacket(receiveBufferSize);
        final byte[] request = new byte[receiveBufferSize];
        while (!datagramSocket.isClosed()) {
            try {
                packet.setData(request);
                datagramSocket.receive(packet);
                final String requestMessage = HelloUDPUtills.getString(packet);
                final byte[] response = HelloUDPUtills.getBytes("Hello, " + requestMessage);
                packet.setData(response, 0, response.length);
                try {
                    datagramSocket.send(packet);
                } catch (final IOException e) {
                    System.out.println("Cant't send DatagramPacket " + e.getMessage());
                }
            } catch (final IOException e) {
                System.out.println("Can't receive request from DatagramSocket " + e.getMessage());
            }
        }
    }

    @Override
    public void start(final int port, final int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        try {
            datagramSocket = new DatagramSocket(port);
            receiveBufferSize = datagramSocket.getReceiveBufferSize();
            for (int i = 0; i < threads; i++) {
                executorService.submit(this::task);
            }
        } catch (final SocketException e) {
            System.out.println("Can't create DatagramSocket " + e.getMessage());
        }
    }

    @Override
    public void close() {
        datagramSocket.close();
        executorService.shutdown();
        try {
            executorService.awaitTermination(100, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            System.out.println("Can't terminate ExecutorService " + e.getMessage());
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Incorrect arguments");
            return;
        }
        final HelloUDPServer server = new HelloUDPServer();
        server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        System.out.println("Enter something to close server");
        new Scanner(System.in).next();
        server.close();
    }
}