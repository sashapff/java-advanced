//package ru.ifmo.rain.ivanova.bank;
//
//import java.io.Serializable;
//import java.util.Map;
//
//class LocalPerson extends AbstractPerson implements Serializable {
//
//    LocalPerson(final long passport, final String firstName, final String lastName,
//                final Map<String, RemoteAccount> accounts) {
//        super(passport, firstName, lastName);
//        accounts.forEach((key, value) -> addAccount(new LocalAccount(value), key));
//    }
//
//    @Override
//    public void addAccount(String id, Account account, final String fullAccountId) {
//            addAccount(new LocalAccount(fullAccountId), id);
//    }
//}
