package ru.ifmo.rain.ivanova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Bank extends Remote {
    /**
     * Creates a new account of person with specified identifier if it is not already exists.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(final String id, final Person person) throws RemoteException;

    /**
     * Returns account by identifier and person.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(final String id, final Person person) throws RemoteException;


    /**
     * Creates a new remote person with specified identifier if it is not already exists.
     *
     * @param passport remote person passport number
     * @return created or existing remote person.
     */
    Person createRemotePerson(final long passport, final String firstName, final String lastName) throws RemoteException;

    /**
     * Creates a new local person with specified identifier if it is not already exists.
     *
     * @param passport local person passport number
     * @return created or existing local person.
     */
    Person createLocalPerson(final long passport, final String firstName, final String lastName) throws RemoteException;

    /**
     * Returns remote person by passport number.
     *
     * @param passport passport number
     * @return person with specified identifier or {@code null} if such person does not exists.
     */
    Person getRemotePerson(final long passport) throws RemoteException;

    /**
     * Returns local person by passport number.
     *
     * @param passport passport number
     * @return person with specified identifier or {@code null} if such person does not exists.
     */
    Person getLocalPerson(final long passport) throws RemoteException;

}
