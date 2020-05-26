//package ru.ifmo.rain.ivanova.bank;
//
//import java.io.UncheckedIOException;
//import java.rmi.Remote;
//import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
//public class RemoteBank implements Bank {
//    private final int port;
//    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
//    private final ConcurrentMap<Integer, Person> persons = new ConcurrentHashMap<>();
//
//    RemoteBank(final int port) {
//        this.port = port;
//    }
//
//    private boolean emptyPerson(final Person person) {
//        if (person == null) {
//            System.out.println("Person cant't be null");
//            return true;
//        }
//        return false;
//    }
//
//    private <T extends Remote> T checkedExport(final T t) {
//        try {
//            UnicastRemoteObject.exportObject(t, port);
//        } catch (RemoteException e) {
//            throw new UncheckedIOException(e);
//        }
//        return t;
//    }
//
//    public Account createAccount(final String id, final Person person) throws RemoteException {
//        if (emptyPerson(person)) {
//            return null;
//        }
//        final String fullAccountId = Utils.getFullAccountId(id, person.getPassport());
//        System.out.println("Creating account " + fullAccountId);
//        try {
//            accounts.computeIfAbsent(fullAccountId, ignored -> {
//                final Account account = new RemoteAccount(fullAccountId);
//                checkedExport(account);
//                try {
//                    Account accountToAdd = account;
//                    if (person instanceof LocalPerson) {
//                        accountToAdd = new RemoteAccount(fullAccountId);
//                    }
//                    person.addAccount(id, accountToAdd);
//                } catch (final RemoteException e) {
//                    throw new UncheckedIOException(e);
//                }
//                return account;
//            });
//        } catch (UncheckedIOException e) {
//            Utils.handleException(e);
//        }
//        return getAccount(id, person);
//    }
//
//    public Account getAccount(final String id, final Person person) throws RemoteException {
//        if (emptyPerson(person)) {
//            return null;
//        }
//        final String fullAccountId = Utils.getFullAccountId(id, person.getPassport());
//        System.out.println("Retrieving account " + fullAccountId);
//        if (person instanceof LocalPerson) {
//            final Account localAccount = person.getAccount(id);
//            return localAccount != null ? localAccount : accounts.get(fullAccountId);
//        }
//        return accounts.get(fullAccountId);
//    }
//
//    @Override
//    public Person createPerson(final int passport, final String firstName, final String lastName) throws RemoteException {
//        System.out.println("Creating person " + passport);
//<<<<<<< HEAD
//=======
//        // :NOTE: Лишние действия
//        final Person person = new RemotePerson(passport, firstName, lastName);
//>>>>>>> 50b2be5fda4ec93ddf7c68c8b97362a6f515902f
//        try {
//            persons.computeIfAbsent(passport, ignored ->
//                    checkedExport(new RemotePerson(passport, firstName, lastName)));
//        } catch (UncheckedIOException e) {
//            Utils.handleException(e);
//        }
//        return persons.get(passport);
//    }
//
//    @Override
//    public Person getRemotePerson(final int passport) {
//        System.out.println("Retrieving remote person " + passport);
//        return persons.get(passport);
//    }
//
//    @Override
//    public Person getLocalPerson(final int passport) throws RemoteException {
//        System.out.println("Retrieving local person " + passport);
//        final Person person = persons.get(passport);
//        return person == null ? null : new LocalPerson(person.getPassport(),
//                person.getFirstName(), person.getLastName(), person.getPersonAccounts());
//    }
//
//}
