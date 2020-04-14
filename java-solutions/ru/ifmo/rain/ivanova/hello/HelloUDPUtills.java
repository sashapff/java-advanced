package ru.ifmo.rain.ivanova.hello;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

class HelloUDPUtills {
    static DatagramPacket newDatagramPacket(int bufferSize) {
        return new DatagramPacket(new byte[bufferSize], bufferSize);
    }

    static DatagramPacket newDatagramPacket(int sendBufferSize, String host, int port) throws UnknownHostException {
        return new DatagramPacket(new byte[sendBufferSize], sendBufferSize,
                new InetSocketAddress(InetAddress.getByName(host), port));
    }

    static byte[] getBytes(String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }

    static String getString(DatagramPacket packet) {
        return new String(packet.getData(),
                packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }
}