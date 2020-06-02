package ru.ifmo.rain.ivanova.bank;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractPerson implements Person {
    private final long passport;
    private final String firstName;
    private final String lastName;
    private final ConcurrentHashMap<String, Account> accounts;

    AbstractPerson(final long passport, final String firstName, final String lastName, final ConcurrentHashMap<String, Account> accounts) {
        this.passport = passport;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = accounts;
    }

    AbstractPerson(final long passport, final String firstName, final String lastName) {
        this(passport, firstName, lastName, new ConcurrentHashMap<>());
    }

    @Override
    public long getPassport() {
        return passport;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void addAccount(final String id, final Account account) {
        accounts.put(id, account);
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }

    @Override
    public Map<String, Account> getPersonAccounts() {
        return Map.copyOf(accounts);
    }

}
