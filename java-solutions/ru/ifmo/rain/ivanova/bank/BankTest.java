package ru.ifmo.rain.ivanova.bank;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class BankTest {
    private static final int PORT = 4040;
    private static Bank bank;
    private static Registry registry;
    private static final int globalPassport = 0;
    private static final String globalFirstName = "First";
    private static final String globalLastName = "Last";
    private static final String globalAccountId = "Account";
    private static final int globalAddition = 50;
    private static final int size = 10;

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        registry = LocateRegistry.createRegistry(PORT);
    }

    @Before
    public void before() throws RemoteException {
        bank = new RemoteBank(PORT);
        final Bank stub = (Bank) UnicastRemoteObject.exportObject(bank, 0);
        registry.rebind("bank", stub);
        System.out.println("New test!");
    }

    private void checkPerson(final Person person) throws RemoteException {
        checkPerson(person, globalPassport, globalFirstName, globalLastName);
    }

    private void checkPerson(final Person person, final int passport,
                             final String firstName, final String lastName) throws RemoteException {
        assertNotNull(person);
        assertEquals(firstName, person.getFirstName());
        assertEquals(lastName, person.getLastName());
        assertEquals(passport, person.getPassport());
    }

    private Person getPerson() throws RemoteException {
        return getPerson(globalPassport, globalFirstName, globalLastName);
    }

    private Person getPerson(final int passport, final String firstName, final String lastName) throws RemoteException {
        final Person person = bank.createPerson(passport, firstName, lastName);
        checkPerson(person, passport, firstName, lastName);
        return person;
    }

    @Test
    public void test00_createPerson() throws RemoteException {
        getPerson();
    }

    private Person getRemotePerson() throws RemoteException {
        return getRemotePerson(globalPassport, globalFirstName, globalLastName);
    }

    private Person getRemotePerson(final int passport, final String firstName,
                                   final String lastName) throws RemoteException {
        getPerson(passport, firstName, lastName);
        final Person remotePerson = bank.getRemotePerson(passport);
        checkPerson(remotePerson, passport, firstName, lastName);
        return remotePerson;
    }

    private Person getLocalPerson() throws RemoteException {
        return getLocalPerson(globalPassport, globalFirstName, globalLastName);
    }

    private Person getLocalPerson(final int passport, final String firstName,
                                  final String lastName) throws RemoteException {
        getPerson(passport, firstName, lastName);
        final Person localPerson = bank.getLocalPerson(passport);
        checkPerson(localPerson, passport, firstName, lastName);
        return localPerson;
    }

    @Test
    public void test01_getRemotePerson() throws RemoteException {
        getRemotePerson();
    }

    @Test
    public void test02_getLocalPerson() throws RemoteException {
        getLocalPerson();
    }

    @Test
    public void test03_createPersonTwice() throws RemoteException {
        final Person person = getPerson();
        assertEquals(person, bank.createPerson(globalPassport, globalFirstName, globalLastName));
    }

    @Test
    public void test04_getNotExistingPerson() throws RemoteException {
        assertNull(bank.getRemotePerson(globalPassport));
        assertNull(bank.getRemotePerson(globalPassport));
    }

    private String toString(final int i) {
        return Integer.toString(i);
    }

    @Test
    public void test05_getManyPersons() throws RemoteException {
        for (int i = 0; i < size; i++) {
            final int passport = i;
            final String firstName = toString(i);
            final String lastName = toString(i + size);
            getRemotePerson(passport, firstName, lastName);
            getLocalPerson(passport, firstName, lastName);
        }
    }

    private String getFullAccountId(final String id, final int passport) {
        return passport + ":" + id;
    }

    private void checkAccount(final Account account, final Person person) throws RemoteException {
        checkAccount(account, person, globalAccountId, globalPassport, 0);
    }

    private void checkAccount(final Account account, final Person person,
                              final String accountId, final int passport, final int amount) throws RemoteException {
        assertNotNull(account);
        assertEquals(account, bank.getAccount(accountId, person));
        assertEquals(amount, account.getAmount());
        if (person instanceof LocalPerson) {
            assertEquals(accountId, account.getId());
        } else {
            assertEquals(getFullAccountId(accountId, passport), account.getId());
        }
    }

    private Account getAccount(Person person) throws RemoteException {
        return getAccount(person, globalAccountId);
    }

    private Account getAccount(Person person, final String accountId) throws RemoteException {
        final Account account = bank.createAccount(accountId, person);
        checkAccount(account, person);
        return account;
    }

    @Test
    public void test06_createAccount() throws RemoteException {
        getAccount(getPerson());
    }

    @Test
    public void test07_getRemotePersonAccount() throws RemoteException {
        getAccount(getRemotePerson());
    }

    @Test
    public void test08_getLocalPersonAccount() throws RemoteException {
        getAccount(getLocalPerson());
    }

    @Test
    public void test09_createAccountTwice() throws RemoteException {
        final Person person = getPerson();
        final Account account = getAccount(person);
        assertEquals(account, bank.createAccount(globalAccountId, person));
    }

    @Test
    public void test10_createRemotePersonAccountTwice() throws RemoteException {
        final Person remotePerson = getRemotePerson();
        final Account account = getAccount(remotePerson);
        assertEquals(account, bank.createAccount(globalAccountId, remotePerson));
    }

    @Test
    public void test11_createLocalPersonAccountTwice() throws RemoteException {
        final Person localPerson = getLocalPerson();
        final Account account = getAccount(localPerson);
        assertEquals(account, bank.createAccount(globalAccountId, localPerson));
    }

    @Test
    public void test12_getNotExistingAccount() throws RemoteException {
        final Person person = getPerson();
        final String accountId = toString(100500);
        assertNull(bank.getAccount(accountId, person));
        final Person remotePerson = getRemotePerson();
        assertNull(bank.getAccount(accountId, remotePerson));
        final Person localPerson = getLocalPerson();
        assertNull(bank.getAccount(accountId, localPerson));
    }

    private void addAccounts(Person person) throws RemoteException {
        addAccounts(person, 0);
    }

    private void addAccounts(Person person, final int flag) throws RemoteException {
        for (int i = 0; i < size; i++) {
            assertEquals(Math.max(i, flag), bank.getAccounts(person).size());
            final String accountId = toString(i);
            final Account account = bank.createAccount(accountId, person);
            checkAccount(account, person, accountId, person.getPassport(), 0);
        }
    }

    private int getPersonAccountsSize(Person person) throws RemoteException {
        return bank.getAccounts(person).size();
    }

    @Test
    public void test13_createManyPersonAccounts() throws RemoteException {
        final Person person = getPerson();
        addAccounts(person);
        addAccounts(person, size);
        assertEquals(size, getPersonAccountsSize(person));
    }

    @Test
    public void test14_addRemotePersonAccount() throws RemoteException {
        final Person remotePerson = getRemotePerson();
        assertEquals(0, getPersonAccountsSize(remotePerson));
        getAccount(remotePerson);
        assertEquals(1, getPersonAccountsSize(remotePerson));
    }

    @Test
    public void test15_addLocalPersonAccount() throws RemoteException {
        final Person localPerson = getLocalPerson();
        assertEquals(0, getPersonAccountsSize(localPerson));
        getAccount(localPerson);
        assertEquals(1, getPersonAccountsSize(localPerson));
    }

    @Test
    public void test16_addManyAccountsToRemotePerson() throws RemoteException {
        addAccounts(getRemotePerson());
    }

    @Test
    public void test17_addManyAccountsToLocalPerson() throws RemoteException {
        addAccounts(getLocalPerson());
    }

    @Test
    public void test18_addManyAccountsToManyRemotePersons() throws RemoteException {
        for (int j = 0; j < size; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + size);
            final Person remotePerson = getRemotePerson(passport, firstName, lastName);
            addAccounts(remotePerson);
        }
    }

    @Test
    public void test19_addManyAccountsToManyLocalPersons() throws RemoteException {
        for (int j = 0; j < size; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + size);
            final Person localPerson = getLocalPerson(passport, firstName, lastName);
            addAccounts(localPerson);
        }
    }

    @Test
    public void test20_getLocalPersonAccountsAddingBeforeCreating() throws RemoteException {
        for (int j = 0; j < size; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + size);
            final Person remotePerson = getRemotePerson(passport, firstName, lastName);
            addAccounts(remotePerson);
            final Person localPerson = getLocalPerson(passport, firstName, lastName);
            assertEquals(size, bank.getAccounts(localPerson).size());
        }
    }

    @Test
    public void test21_getLocalPersonAccountsAddingAfterCreating() throws RemoteException {
        for (int j = 0; j < size; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + size);
            final Person remotePerson = getRemotePerson(passport, firstName, lastName);
            final Person localPerson = getLocalPerson(passport, firstName, lastName);
            addAccounts(remotePerson);
            assertEquals(0, bank.getAccounts(localPerson).size());
        }
    }

    private void setAmount(Account account) throws RemoteException {
        setAmount(account, globalAddition);
    }

    private void setAmount(Account account, final int addition) throws RemoteException {
        account.setAmount(addition);
        assertEquals(globalAddition, account.getAmount());
    }

    @Test
    public void test22_setAmountInLocalAccountCreatingLocalAccountAfterLocalPerson() throws RemoteException {
        final Person remotePerson = getRemotePerson();
        final Person localPerson = getLocalPerson();
        final Account localAccount = getAccount(localPerson);
        final Account remoteAccount = getAccount(remotePerson);
        setAmount(localAccount);
        assertEquals(0, remoteAccount.getAmount());
    }

    @Test
    public void test23_setAmountInRemoteAccountCreatingRemoteAccountAfterLocalPerson() throws RemoteException {
        final Person remotePerson = getRemotePerson();
        final Person localPerson = getLocalPerson();
        final Account remoteAccount = getAccount(remotePerson);
        final Account localAccount = bank.getAccount(globalAccountId, localPerson);
        assertNotNull(localAccount);
        assertEquals(localAccount, remoteAccount);
        remoteAccount.setAmount(globalAddition);
        assertEquals(globalAddition, remoteAccount.getAmount());
        assertEquals(globalAddition, localAccount.getAmount());
    }

    @Test
    public void test24_setAmountInRemoteAccountCreatingRemoteAccountBeforeLocalPerson() throws RemoteException {
        final Person remotePerson = getRemotePerson();
        final Account remoteAccount = bank.createAccount(globalAccountId, remotePerson);
        assertNotNull(remoteAccount);
        Person localPerson = getLocalPerson();
        Account localAccount = bank.getAccount(globalAccountId, localPerson);
        assertNotNull(localAccount);
        remoteAccount.setAmount(globalAddition);
        assertEquals(getFullAccountId(globalAccountId, remotePerson.getPassport()), remoteAccount.getId());
        assertEquals(globalAddition, remoteAccount.getAmount());
        localAccount = bank.getAccount(globalAccountId, localPerson);
        assertNotNull(localAccount);
        assertEquals(0, localAccount.getAmount());
        localPerson = bank.getLocalPerson(globalPassport);
        localAccount = bank.getAccount(globalAccountId, localPerson);
        assertEquals(globalAddition, localAccount.getAmount());
    }

    @Test
    public void test25_setAmountOfLocalAccountCreatingRemoteAccountAfterLocalPerson() throws RemoteException {
        final Person localPerson = getLocalPerson();
        Account localAccount = bank.createAccount(globalAccountId, localPerson);
        assertNotNull(localAccount);
        localAccount.setAmount(globalAddition);
        assertEquals(globalAddition, localAccount.getAmount());
        localAccount = bank.getAccount(globalAccountId, localPerson);
        assertNotNull(localAccount);
        assertEquals(globalAddition, localAccount.getAmount());
        final Person remotePerson = getRemotePerson();
        final Account remoteAccount = bank.getAccount(globalAccountId, remotePerson);
        assertNotNull(remoteAccount);
        assertEquals(0, remoteAccount.getAmount());
    }

    @Test
    public void test26_manyAccountsOfOneRemotePerson() throws RemoteException {
        final Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        final List<Person> persons = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            persons.add(bank.getRemotePerson(globalPassport));
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                assertNotNull(bank.createAccount(i + "+" + j, persons.get(j)));
            }
        }
        for (int i = 0; i < size; i++) {
            assertEquals(size * size, bank.getAccounts(persons.get(i)).size());
        }
    }

    @Test
    public void test27_setAmountOfManyAccountsOfManyPersons() throws RemoteException {
        final List<Person> persons = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final Person person = bank.createPerson(i, globalFirstName, globalLastName);
            assertNotNull(person);
            persons.add(bank.getRemotePerson(i));
        }
        final Map<String, Integer> answer = new HashMap<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                final String accountId = i + "+" + j;
                final Account account = bank.createAccount(accountId, persons.get(j));
                assertNotNull(account);
                account.setAmount(i + j);
                answer.put(accountId, i + j);
            }
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                final String accountId = i + "+" + j;
                assertEquals((int) answer.get(accountId), bank.getAccount(accountId, persons.get(j)).getAmount());
            }
        }
    }

    @Test
    public void test28_setAmountTwice() throws RemoteException {
        for (var i = 0; i < size; ++i) {
            final Person person = bank.createPerson(globalPassport + i,
                    globalFirstName + i, globalLastName + i);
            assertNotNull(person);
        }

        for (var i = 0; i < size; i++) {
            final Person remotePerson = bank.getRemotePerson(globalPassport + i);
            for (var j = 0; j < size; j++) {
                bank.createAccount("Account" + j, remotePerson);
            }
        }

        for (var i = 0; i < size; i++) {
            final Person person = bank.getRemotePerson(globalPassport + i);
            for (var j = 0; j < size; j++) {
                final Account account = bank.getAccount("Account" + j, person);
                assertNotNull(account);
                account.setAmount(account.getAmount() + globalAddition);
                assertEquals(globalAddition, account.getAmount());
            }
        }

        for (var i = 0; i < size; i++) {
            final Person person = bank.getRemotePerson(globalPassport + i);
            for (var j = 0; j < size; j++) {
                final Account account = bank.getAccount("Account" + j, person);
                assertNotNull(account);
                account.setAmount(account.getAmount() - globalAddition);
                assertEquals(0, account.getAmount());
            }
        }
    }

    @Test
    public void test29_client() throws RemoteException {
        Client.main(globalFirstName, globalLastName,
                toString(globalPassport), globalAccountId, toString(globalAddition));
        final Person person = bank.getRemotePerson(globalPassport);
        checkPerson(person);
        final Account account = bank.getAccount(globalAccountId, person);
        checkAccount(account, person, globalAccountId, globalPassport, globalAddition);

        Client.main(globalFirstName + "incorrect", globalLastName + "incorrect",
                toString(globalPassport), globalAccountId, toString(globalAddition));
        checkPerson(person);
        checkAccount(account, person, globalAccountId, globalPassport, globalAddition);

        Client.main(globalFirstName, globalLastName,
                toString(globalPassport), globalAccountId, toString(globalAddition));
        checkPerson(person);
        checkAccount(account, person, globalAccountId, globalPassport, 2 * globalAddition);

        Client.main(globalFirstName, globalLastName,
                toString(globalPassport), globalAccountId);
        checkPerson(person);
        checkAccount(account, person, globalAccountId, globalPassport, 2 * globalAddition);

    }

}
