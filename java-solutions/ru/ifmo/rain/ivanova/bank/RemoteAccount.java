package ru.ifmo.rain.ivanova.bank;

import java.rmi.RemoteException;

public class RemoteAccount implements Account {
    private final String id;
    private int amount;

    RemoteAccount(final String id) {
        this(id, 0);
    }

    RemoteAccount(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    RemoteAccount(final Account account) throws RemoteException {
        this(account.getId(), account.getAmount());
    }

    public String getId() {
        return id;
    }

    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    public synchronized void addAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount += amount;
    }
}
