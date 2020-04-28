package ru.ifmo.rain.ivanova.bank;

public class PersonAccount implements Account {
    private final String id;
    private int amount;

    public PersonAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    public PersonAccount(String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
