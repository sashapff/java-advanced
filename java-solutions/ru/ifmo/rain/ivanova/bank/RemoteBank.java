package ru.ifmo.rain.ivanova.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Set<String>> personAccounts = new ConcurrentHashMap<>();

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

    public Account createAccount(final String id, Person person) throws RemoteException {
        if (emptyPerson(person)) {
            return null;
        }
        String fullAccountId = getFullAccountId(id, person.getPassport());
        System.out.println("Creating account " + fullAccountId);
        final Account account = new RemoteAccount(fullAccountId);
        if (accounts.putIfAbsent(fullAccountId, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            Set<String> acs = personAccounts.get(person.getPassport());
            if (acs == null) {
                System.out.println("Person doesn't exist");
                return null;
            }
            acs.add(id);
            if (person instanceof LocalPerson) {
                ((LocalPerson) person).addAccount(id, new RemoteAccount(id));
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
        Account account = accounts.get(fullAccountId);
        if (account == null) {
            System.out.println("Account doesn't exist");
            return null;
        }
        if (person instanceof LocalPerson) {
            Account localAccount = ((LocalPerson) person).getAccount(id);
            if (localAccount != null) {
                return localAccount;
            }
        }
        return account;
    }

    @Override
    public Person createPerson(int passport, String firstName, String lastName) throws RemoteException {
        System.out.println("Creating person " + passport);
        final Person person = new RemotePerson(passport, firstName, lastName);
        if (persons.putIfAbsent(passport, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            personAccounts.put(passport, new ConcurrentSkipListSet<>());
            return person;
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
        if (person == null) {
            return null;
        }
        Map<String, Account> localAccounts = new HashMap<>();
        getAccounts(person).forEach(i -> {
            try {
                Account account = getAccount(i, person);
                localAccounts.put(i, new RemoteAccount(i, account.getAmount()));
            } catch (RemoteException e) {
                System.out.println("Can't get account " + e.getMessage());
            }
        });
        return new LocalPerson(passport, person.getFirstName(), person.getLastName(), localAccounts);
    }

    @Override
    public Set<String> getAccounts(Person person) throws RemoteException {
        if (person instanceof LocalPerson) {
            return ((LocalPerson) person).getAccounts();
        }
        return personAccounts.get(person.getPassport());
    }

}
