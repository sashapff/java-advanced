package ru.ifmo.rain.ivanova.bank;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class LocalPerson implements Person, Serializable {
    private final int passport;
    private final String firstName;
    private final String lastName;
    private final Map<String, RemoteAccount> accounts;

    public LocalPerson(int passport, String firstName, String lastName, Map<String, RemoteAccount> accounts) {
        this.passport = passport;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = accounts;
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

    void addAccount(final String id, final RemoteAccount account) {
        accounts.put(id, account);
    }

    Account getAccount(final String id) {
        return accounts.get(id);
    }

    Set<String> getAccounts() {
        return accounts.keySet();
    }
}
