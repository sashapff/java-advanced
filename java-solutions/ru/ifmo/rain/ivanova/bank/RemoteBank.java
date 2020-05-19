package ru.ifmo.rain.ivanova.bank;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();

    RemoteBank(final int port) {
        this.port = port;
    }

    private String getFullAccountId(final String id, final int password) {
        return password + ":" + id;
    }

    private boolean emptyPerson(Person person) {
        if (person == null) {
            System.out.println("Person cant't be null");
            return true;
        }
        return false;
    }

    private <T extends Remote> void exportObject(final T o, final int port) {
        try {
            UnicastRemoteObject.exportObject(o, port);
        } catch (RemoteException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void throwException(final UncheckedIOException e) throws RemoteException {
        IOException cause = e.getCause();
        if (cause instanceof RemoteException) {
            throw (RemoteException) cause;
        } else {
            throw e;
        }
    }

    public Account createAccount(final String id, Person person) throws RemoteException {
        if (emptyPerson(person)) {
            return null;
        }
        String fullAccountId = getFullAccountId(id, person.getPassport());
        System.out.println("Creating account " + fullAccountId);
        final Account account = new RemoteAccount(fullAccountId);
        if (accounts.putIfAbsent(fullAccountId, account) == null) {
            try {
                exportObject(account, port);
                if (person instanceof LocalPerson) {
                    person.addAccount(id, new RemoteAccount(account.getId()));
                } else {
                    person.addAccount(id, account);
                }
            } catch (UncheckedIOException e) {
                throwException(e);
            }
        }
        return getAccount(id, person);
    }

    public Account getAccount(final String id, Person person) throws RemoteException {
        if (emptyPerson(person)) {
            return null;
        }
        String fullAccountId = getFullAccountId(id, person.getPassport());
        System.out.println("Retrieving account " + fullAccountId);
        if (person instanceof LocalPerson) {
            Account localAccount = person.getAccount(id);
            if (localAccount != null) {
                return localAccount;
            }
        }
        Account account = accounts.get(fullAccountId);
        if (account == null) {
            System.out.println("Account doesn't exist");
            return null;
        }
        return account;
    }

    @Override
    public Person createPerson(int passport, String firstName, String lastName) throws RemoteException {
        System.out.println("Creating person " + passport);
        final Person person = new RemotePerson(passport, firstName, lastName);
        if (persons.putIfAbsent(passport, person) == null) {
            try {
                exportObject(person, port);
                return person;
            } catch (UncheckedIOException e) {
                throwException(e);
            }
        }
        return persons.get(passport);
    }

    @Override
    public Person getRemotePerson(int passport) {
        System.out.println("Retrieving remote person " + passport);
        return persons.get(passport);
    }

    @Override
    public Person getLocalPerson(int passport) throws RemoteException {
        System.out.println("Retrieving local person " + passport);
        Person person = persons.get(passport);
        return person == null ? null : new LocalPerson(person.getPassport(),
                person.getFirstName(), person.getLastName(), person.getPersonAccounts());
    }

}
