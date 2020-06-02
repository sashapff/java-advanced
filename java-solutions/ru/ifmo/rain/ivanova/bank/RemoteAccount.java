package ru.ifmo.rain.ivanova.bank;

import java.rmi.RemoteException;

public class RemoteAccount extends AbstractAccount {

    RemoteAccount(final String id) {
        super(id);
    }

    RemoteAccount(final String id, final long amount) {
        super(id, amount);
    }

    RemoteAccount(final Account account) throws RemoteException {
        super(account);
    }

}
