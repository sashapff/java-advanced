package ru.ifmo.rain.ivanova.bank;

import java.rmi.RemoteException;

abstract class AbstractAccount implements Account {
    private final String id;
    private long amount;

    AbstractAccount(final String id) {
        this(id, 0);
    }

    AbstractAccount(final String id, final long amount) {
        this.id = id;
        this.amount = amount;
    }

    AbstractAccount(final RemoteAccount account)  {
        this(account.getId(), account.getAmount());
    }

    public String getId() {
        return id;
    }

    public long getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    public synchronized void addAmount(final long amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount += amount;
    }
}
