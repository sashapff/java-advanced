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
    Account createAccount(String id, Person person) throws RemoteException;

    /**
     * Returns account by identifier and person.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id, Person person) throws RemoteException;


    /**
     * Creates a new person with specified identifier if it is not already exists.
     *
     * @param passport person passport number
     * @return created or existing person.
     */
    Person createPerson(int passport, String firstName, String lastName) throws RemoteException;

    /**
     * Returns remote person by passport number.
     *
     * @param passport passport number
     * @return person with specified identifier or {@code null} if such person does not exists.
     */
    Person getRemotePerson(int passport) throws RemoteException;

    /**
     * Returns local person by passport number.
     *
     * @param passport passport number
     * @return person with specified identifier or {@code null} if such person does not exists.
     */
    Person getLocalPerson(int passport) throws RemoteException;

    /**
     * Returns accounts by person.
     *
     * @param person person to get accounts.
     * @return acconts of person.
     */
    Set<String> getAccounts(Person person) throws RemoteException;
}
