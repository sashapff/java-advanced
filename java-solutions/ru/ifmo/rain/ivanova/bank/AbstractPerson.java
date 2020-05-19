package ru.ifmo.rain.ivanova.bank;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractPerson implements Person {
    final int passport;
    final String firstName;
    final String lastName;
    final ConcurrentHashMap<String, Account> accounts;

    AbstractPerson(int passport, String firstName, String lastName, ConcurrentHashMap<String, Account> accounts) {
        this.passport = passport;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = accounts;
    }

    AbstractPerson(int passport, String firstName, String lastName) {
        this.passport = passport;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public int getPassport() {
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
    public void addAccount(String id, Account account) {
        accounts.putIfAbsent(id, account);
    }

    @Override
    public Account getAccount(String id) {
        return accounts.get(id);
    }

    @Override
    public Map<String, Account> getPersonAccounts() {
        return Map.copyOf(accounts);
    }

}
