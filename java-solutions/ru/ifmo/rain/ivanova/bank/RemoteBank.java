package ru.ifmo.rain.ivanova.bank;

import java.io.UncheckedIOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Person> persons = new ConcurrentHashMap<>();

    RemoteBank(final int port) {
        this.port = port;
    }

    private boolean emptyPerson(final Person person) {
        if (person == null) {
            System.out.println("Person cant't be null");
            return true;
        }
        return false;
    }

    private <T extends Remote> T checkedExport(final T t) {
        try {
            UnicastRemoteObject.exportObject(t, port);
        } catch (RemoteException e) {
            throw new UncheckedIOException(e);
        }
        return t;
    }

    public Account createAccount(final String id, final Person person) throws RemoteException {
        if (emptyPerson(person)) {
            return null;
        }
        final String fullAccountId = Utils.getFullAccountId(id, person.getPassport());
        System.out.println("Creating account " + fullAccountId);
        try {
            accounts.computeIfAbsent(fullAccountId, ignored -> {
                final Account account = new RemoteAccount(fullAccountId);
                checkedExport(account);
                try {
                    if (person instanceof LocalPerson) {
                        person.addAccount(id, new LocalAccount(fullAccountId));
                    } else {
                        person.addAccount(id, account);
                    }
                } catch (final RemoteException e) {
                    throw new UncheckedIOException(e);
                }
                return account;
            });
        } catch (UncheckedIOException e) {
            Utils.handleException(e);
        }
        return getAccount(id, person);
    }

    public Account getAccount(final String id, final Person person) throws RemoteException {
        if (emptyPerson(person)) {
            return null;
        }
        final String fullAccountId = Utils.getFullAccountId(id, person.getPassport());
        System.out.println("Retrieving account " + fullAccountId);
        if (person instanceof LocalPerson) {
            final Account account = person.getAccount(id);
            if (account != null) {
                return account;
            }
        }
        return accounts.get(fullAccountId);
    }

    private void createPerson(final long passport, final String firstName, final String lastName) throws RemoteException {
        System.out.println("Creating person " + passport);
        try {
            persons.computeIfAbsent(passport, ignored ->
                    checkedExport(new RemotePerson(passport, firstName, lastName)));
        } catch (UncheckedIOException e) {
            Utils.handleException(e);
        }
    }

    @Override
    public Person createRemotePerson(final long passport, final String firstName, final String lastName) throws RemoteException {
        createPerson(passport, firstName, lastName);
        return persons.get(passport);
    }

    @Override
    public Person createLocalPerson(final long passport, final String firstName, final String lastName) throws RemoteException {
        createPerson(passport, firstName, lastName);
        return new LocalPerson(passport, firstName, lastName, new ConcurrentHashMap<>());
    }

    @Override
    public Person getRemotePerson(final long passport) {
        System.out.println("Retrieving remote person " + passport);
        final Person person = persons.get(passport);
        if (person == null) {
            return null;
        }
        return person;
    }

    @Override
    public Person getLocalPerson(final long passport) throws RemoteException {
        System.out.println("Retrieving local person " + passport);
        final Person person = persons.get(passport);
        return person instanceof LocalPerson
                ? person
                : new LocalPerson(person.getPassport(), person.getFirstName(), person.getLastName(), person.getPersonAccounts());
    }

}