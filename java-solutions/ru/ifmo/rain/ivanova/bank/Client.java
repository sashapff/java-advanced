package ru.ifmo.rain.ivanova.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Objects;

public class Client {
    private static int parseArgument(String arg) {
        return Integer.parseInt(arg);
    }

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            Registry registry = LocateRegistry.getRegistry(4040);
            bank = (Bank) registry.lookup("bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        }

        if (args.length != 5 || !Arrays.stream(args).allMatch(Objects::nonNull)) {
            System.out.println("Arguments need to be not null and have format: " +
                    "<firstName> <secondName> <passport> <accountId> <addition>");
            return;
        }

        String firstName = args[0];
        String lastName = args[1];
        int passport = parseArgument(args[2]);
        String accountId = args[3];
        int addition = parseArgument(args[4]);

        Person person = bank.getRemotePerson(passport);
        if (person == null) {
            System.out.println("Creating person");
            person = bank.createPerson(passport, firstName, lastName);
        } else {
            if (!person.getFirstName().equals(firstName) || !person.getLastName().equals(lastName)) {
                System.out.println("Incorrect name of person");
                return;
            }
            System.out.println("Person already exists");
        }

        Account account = bank.getAccount(accountId, person);
        if (account == null) {
            System.out.println("Creating account");
            account = bank.createAccount(accountId, person);
        } else {
            System.out.println("Account already exists");
        }

        account.addAmount(addition);

        System.out.println("Person first name: " + person.getFirstName());
        System.out.println("Person last name: " + person.getLastName());
        System.out.println("Person passport number: " + person.getPassport());
        System.out.println("Account id: " + account.getId());
        System.out.println("Adding money: " + addition);
        System.out.println("Total money: " + account.getAmount());
    }
}
