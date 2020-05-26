package ru.ifmo.rain.ivanova.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

class LocalPerson extends AbstractPerson implements Serializable {

    LocalPerson(final int passport, final String firstName, final String lastName,
                final Map<String, Account> accounts) {
        super(passport, firstName, lastName);
        accounts.forEach((key, value) -> {
            try {
                addAccount(key, new RemoteAccount(value));
            } catch (RemoteException e) {
                System.out.println("Can't get account " + e.getMessage());
            }
        });
    }

}
