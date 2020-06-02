package ru.ifmo.rain.ivanova.bank;

import java.rmi.RemoteException;

public class LocalAccount extends AbstractAccount {
    public LocalAccount(String id) {
        super(id);
    }

    public LocalAccount(String id, long amount) {
        super(id, amount);
    }

    public LocalAccount(Account account) throws RemoteException {
        super(account);
    }
}
