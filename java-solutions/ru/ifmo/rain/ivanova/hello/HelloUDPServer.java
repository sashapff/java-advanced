package ru.ifmo.rain.ivanova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket datagramSocket;
    private ExecutorService executorService;
    private int receiveBufferSize;

    private void task() {
        while (!datagramSocket.isClosed()) {
            final DatagramPacket packet = HelloUDPUtills.newDatagramPacket(receiveBufferSize);
            try {
                datagramSocket.receive(packet);
                String requestMessage = HelloUDPUtills.getString(packet);
                String responseMessage = "Hello, " + requestMessage;
                packet.setData(HelloUDPUtills.getBytes(responseMessage));
                try {
                    datagramSocket.send(packet);
                } catch (IOException e) {
                    System.out.println("Cant't send DatagramPacket " + e.getMessage());
                }
            } catch (IOException e) {
                System.out.println("Can't receive request from DatagramSocket " + e.getMessage());
            }
        }
    }

    @Override
    public void start(int port, int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        try {
            datagramSocket = new DatagramSocket(port);
            receiveBufferSize = datagramSocket.getReceiveBufferSize();
            for (int i = 0; i < threads; i++) {
                executorService.submit(this::task);
            }
        } catch (SocketException e) {
            datagramSocket.close();
            System.out.println("Can't create DatagramSocket " + e.getMessage());
        }
    }

    @Override
    public void close() {
        datagramSocket.close();
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Can't terminate ExecutorService " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Incorrect arguments");
            return;
        }
        new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
}