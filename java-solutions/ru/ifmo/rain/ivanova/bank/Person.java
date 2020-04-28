package ru.ifmo.rain.ivanova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    /** Returns person passport number. */
    int getPassport() throws RemoteException;

    /** Returns person first name. */
    String getFirstName() throws RemoteException;

    /** Returns person second name. */
    String getLastName() throws RemoteException;

}
