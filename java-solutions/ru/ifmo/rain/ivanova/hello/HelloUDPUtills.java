package ru.ifmo.rain.ivanova.hello;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;

class HelloUDPUtills {
    static DatagramPacket newDatagramPacket(final int bufferSize) {
        return new DatagramPacket(new byte[bufferSize], bufferSize);
    }

    static DatagramPacket newDatagramPacket(final byte[] response, final String host, final int port) throws UnknownHostException {
        return new DatagramPacket(response, response.length,
                new InetSocketAddress(InetAddress.getByName(host), port));
    }

    static byte[] getBytes(final String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }

    private static String getString(final DatagramPacket packet) {
        return new String(packet.getData(),
                packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    static String receive(final DatagramSocket datagramSocket, final DatagramPacket packet, final byte[] response) throws IOException {
        packet.setData(response, 0, response.length);
        datagramSocket.receive(packet);
        return HelloUDPUtills.getString(packet);
    }

    static void send(final DatagramSocket datagramSocket, final DatagramPacket packet, final byte[] request) throws IOException {
        packet.setData(request, 0, request.length);
        datagramSocket.send(packet);
    }

    static String decode(final ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    static class Pair {
        boolean result;
        int i;

        Pair(final boolean result, final int i) {
            this.result = result;
            this.i = i;
        }
    }

    private static int skipChars(final String s, int i) {
        while (i < s.length() && !Character.isDigit(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static boolean checkSubStr(final String s, final String as, final int i) {
        return !s.substring(i, i + as.length()).equals(as);
    }

    private static Pair checkInt(int i, final String s, final String as) {
        i = skipChars(s, i);
        if (i == s.length() || checkSubStr(s, as, i)) {
            return new Pair(false, i);
        }
        i += as.length();
        return new Pair(true, i);
    }

    static boolean checkInts(final String s, final int a, final int b) {
        final int i = 0;
        final Pair ap = checkInt(i, s, Integer.toString(a));
        final Pair bp = checkInt(ap.i, s, Integer.toString(b));
        if (!bp.result || !ap.result) {
            return false;
        }
        return skipChars(s, bp.i) == s.length();
    }

    static void changeInterestToRead(final SelectionKey key, final Selector selector) {
        key.interestOpsOr(SelectionKey.OP_READ);
        selector.wakeup();
    }

    static void changeInterestToWrite(final SelectionKey key, final Selector selector) {
        key.interestOpsOr(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    static void changeInterestFromRead(final SelectionKey key, final Selector selector) {
        key.interestOpsAnd(~SelectionKey.OP_READ);
        selector.wakeup();
    }

    static void changeInterestFromWrite(final SelectionKey key, final Selector selector) {
        key.interestOpsAnd(~SelectionKey.OP_WRITE);
        selector.wakeup();
    }

}
