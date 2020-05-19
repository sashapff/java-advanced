package ru.ifmo.rain.ivanova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {
    /** Returns person passport number. */
    int getPassport() throws RemoteException;

    /** Returns person first name. */
    String getFirstName() throws RemoteException;

    /** Returns person second name. */
    String getLastName() throws RemoteException;

    void addAccount(final String id, final Account account) throws RemoteException;

    Account getAccount(final String id) throws RemoteException;

    Map<String, Account> getPersonAccounts() throws RemoteException;

}
