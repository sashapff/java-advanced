package ru.ifmo.rain.ivanova.bank;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class LocalPerson extends PersonImpl implements Serializable {
    private final Map<String, Account> accounts;

    LocalPerson(final int passport, final String firstName, final String lastName,
                final Map<String, Account> accounts) {
        super(passport, firstName, lastName);
        this.accounts = accounts;
    }

    void addAccount(final String id, final Account account) {
        accounts.put(id, account);
    }

    Account getAccount(final String id) {
        return accounts.get(id);
    }

    Set<String> getAccounts() {
        return accounts.keySet();
    }
}
