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
    private int bufferSize;

    private void task() {
        final DatagramPacket packet = HelloUDPUtills.newDatagramPacket(bufferSize);
        final byte[] request = new byte[bufferSize];
        while (!datagramSocket.isClosed()) {
            try {
                final String message = HelloUDPUtills.receive(datagramSocket, packet, request);
                final byte[] response = HelloUDPUtills.getBytes("Hello, " + message);
                try {
                    HelloUDPUtills.send(datagramSocket, packet, response);
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
            bufferSize = datagramSocket.getReceiveBufferSize();
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
        HelloUDPUtills.closeExecutorService(executorService);
    }

    public static void main(final String[] args) {
        try (final HelloUDPServer server = new HelloUDPServer()) {
            HelloUDPUtills.main(args, server);
        }
    }
}