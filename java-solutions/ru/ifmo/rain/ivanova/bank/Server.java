//package ru.ifmo.rain.ivanova.bank;
//
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//
//public class Server {
//    private final static int PORT = 4040;
//
//    public static void main(final String... args) {
//        final Bank bank = new RemoteBank(PORT);
//        try {
//            final Registry registry = LocateRegistry.createRegistry(1489);
//            registry.rebind("bank", bank);
//        } catch (final RemoteException e) {
//            System.out.println("Cannot export object: " + e.getMessage());
//            e.printStackTrace();
//        }
//        System.out.println("Server started");
//    }
//}
