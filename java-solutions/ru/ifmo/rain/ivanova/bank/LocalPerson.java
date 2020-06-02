package ru.ifmo.rain.ivanova.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

class LocalPerson extends AbstractPerson implements Serializable {

    LocalPerson(final long passport, final String firstName, final String lastName,
                final Map<String, Account> accounts) {
        super(passport, firstName, lastName);
        accounts.forEach((key, value) -> {
            try {
                addAccount(new LocalAccount(value), key);
            } catch (RemoteException e) {
                System.out.println("Can't add account " + e.getMessage());
            }
        });
    }

    @Override
    public void addAccount(String id, Account account, final String fullAccountId) {
            addAccount(new LocalAccount(fullAccountId), id);
    }
}
