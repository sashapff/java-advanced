package ru.ifmo.rain.ivanova.bank;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class BankTest {
    private static final int PORT = 4040;
    private static Bank bank;
    private static Registry registry;
    private static final long GLOBAL_PASSPORT = 0;
    private static final String GLOBAL_FIRST_NAME = "First";
    private static final String GLOBAL_LAST_NAME = "Last";
    private static final String GLOBAL_ACCOUNT_ID = "Account";
    private static final long GLOBAL_ADDITION = 50;
    private static final int SIZE = 10;
    private static final int THREADS = 10;

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
        checkPerson(person, GLOBAL_PASSPORT, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
    }

    private void checkPerson(final Person person, final long passport,
                             final String firstName, final String lastName) throws RemoteException {
        assertNotNull(person);
        assertEquals(firstName, person.getFirstName());
        assertEquals(lastName, person.getLastName());
        assertEquals(passport, person.getPassport());
    }

    private Person createRemotePerson() throws RemoteException {
        return createRemotePerson(GLOBAL_PASSPORT, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
    }

    private Person createRemotePerson(final long passport, final String firstName,
                                      final String lastName) throws RemoteException {
        final Person remotePerson = bank.createRemotePerson(passport, firstName, lastName);
        checkPerson(remotePerson, passport, firstName, lastName);
        return remotePerson;
    }

    private Person createLocalPerson() throws RemoteException {
        return createLocalPerson(GLOBAL_PASSPORT, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
    }

    private Person createLocalPerson(final long passport, final String firstName,
                                     final String lastName) throws RemoteException {
        final Person localPerson = bank.createLocalPerson(passport, firstName, lastName);
        checkPerson(localPerson, passport, firstName, lastName);
        return localPerson;
    }

    @Test
    public void test01_createRemotePerson() throws RemoteException {
        createRemotePerson();
    }

    @Test
    public void test02_createLocalPerson() throws RemoteException {
        createLocalPerson();
    }

    private void checkPersons(final Person person, final Person samePerson) throws RemoteException {
        assertEquals(person.getPassport(), samePerson.getPassport());
        assertEquals(person.getFirstName(), samePerson.getFirstName());
        assertEquals(person.getLastName(), samePerson.getLastName());
        assertEquals(person.getPersonAccounts(), samePerson.getPersonAccounts());
    }

    @Test
    public void test03_createPersonTwice() throws RemoteException {
        final Person person = createRemotePerson();
        final Person samePerson = createRemotePerson();
        checkPersons(person, samePerson);
    }

    @Test
    public void test04_getNotExistingPerson() throws RemoteException {
        assertNull(bank.getRemotePerson(GLOBAL_PASSPORT));
        assertNull(bank.getRemotePerson(GLOBAL_PASSPORT));
    }

    private String toString(final long i) {
        return Long.toString(i);
    }

    private Person getRemotePerson(final long passport) throws RemoteException {
        return bank.getRemotePerson(passport);
    }

    private Person getLocalPerson(final long passport) throws RemoteException {
        return bank.getLocalPerson(passport);
    }

    @Test
    public void test05_createAndGetManyPersons() throws RemoteException {
        for (int i = 0; i < SIZE; i++) {
            final long passport = i;
            final String firstName = toString(i);
            final String lastName = toString(i + SIZE);
            createRemotePerson(passport, firstName, lastName);
            createLocalPerson(passport, firstName, lastName);
        }
        for (int i = 0; i < SIZE; i++) {
            final long passport = i;
            final String firstName = toString(i);
            final String lastName = toString(i + SIZE);
            checkPerson(getRemotePerson(passport), passport, firstName, lastName);
            checkPerson(getLocalPerson(passport), passport, firstName, lastName);
        }
    }

    private String getFullAccountId(final String id, final long passport) {
        return passport + ":" + id;
    }

    private void checkAccount(final Account account, final String accountId,
                              final Person person) throws RemoteException {
        final String fullAccountId = getFullAccountId(accountId, person.getPassport());
        assertNotNull(account);
        assertEquals(fullAccountId, account.getId());
        Account personAccount = bank.getAccount(accountId, person);
        assertNotNull(personAccount);
        assertEquals(personAccount.getId(), account.getId());
        assertEquals(personAccount.getAmount(), account.getAmount());
    }

    private Account createAccount(final Person person) throws RemoteException {
        return createAccount(person, GLOBAL_ACCOUNT_ID);
    }

    private Account createAccount(final Person person, final String accountId) throws RemoteException {
        final Account account = bank.createAccount(accountId, person);
        checkAccount(account, accountId, person);
        return account;
    }

    @Test
    public void test06_createPersonAccount() throws RemoteException {
        createAccount(createRemotePerson());
    }

    @Test
    public void test07_createRemotePersonAccount() throws RemoteException {
        createAccount(createRemotePerson());
    }

    @Test
    public void test08_createLocalPersonAccount() throws RemoteException {
        createAccount(createLocalPerson());
    }

    private void checkAccounts(final Account account, final Account sameAccount) throws RemoteException {
        assertEquals(account.getId(), sameAccount.getId());
        assertEquals(account.getAmount(), sameAccount.getAmount());
    }

    private void createAccountTwice(final Person person) throws RemoteException {
        final Account account = createAccount(person);
        final Account sameAccount = createAccount(person);
        checkAccounts(account, sameAccount);
    }

    @Test
    public void test09_createAccountTwice() throws RemoteException {
        createAccountTwice(createRemotePerson());
    }

    @Test
    public void test10_createRemotePersonAccountTwice() throws RemoteException {
        createAccountTwice(createRemotePerson());
    }

    @Test
    public void test11_createLocalPersonAccountTwice() throws RemoteException {
        createAccountTwice(createLocalPerson());
    }

    @Test
    public void test12_getNotExistingAccount() throws RemoteException {
        assertNull(bank.getAccount("100500", createRemotePerson()));
        assertNull(bank.getAccount("100501", createRemotePerson()));
        assertNull(bank.getAccount("100502", createLocalPerson()));
    }

    private void addAccounts(final Person person) throws RemoteException {
        addAccounts(person, 0);
    }

    private int getPersonAccountsSize(final Person person) throws RemoteException {
        return person.getPersonAccounts().size();
    }

    private void addAccounts(final Person person, final int flag) throws RemoteException {
        for (int i = 0; i < SIZE; i++) {
            assertEquals(Math.max(i, flag), getPersonAccountsSize(person));
            final String accountId = toString(i);
            checkAccount(createAccount(person, accountId), accountId, person);
        }
    }

    @Test
    public void test13_createManyPersonAccounts() throws RemoteException {
        final Person person = createRemotePerson();
        addAccounts(person);
        assertEquals(SIZE, getPersonAccountsSize(person));
        addAccounts(person, SIZE);
    }

    private void checkPersonAccountsSize(final Person person, final int size) throws RemoteException {
        assertNotNull(person);
        assertEquals(size, getPersonAccountsSize(person));
    }

    private void addPersonAccount(final Person person) throws RemoteException {
        checkPersonAccountsSize(person, 0);
        createAccount(person);
        checkPersonAccountsSize(person, 1);
    }

    @Test
    public void test14_addRemotePersonAccount() throws RemoteException {
        addPersonAccount(createRemotePerson());
    }

    @Test
    public void test15_addLocalPersonAccount() throws RemoteException {
        addPersonAccount(createLocalPerson());
    }

    @Test
    public void test16_addManyAccountsToRemotePerson() throws RemoteException {
        addAccounts(createRemotePerson());
    }

    @Test
    public void test17_addManyAccountsToLocalPerson() throws RemoteException {
        addAccounts(createLocalPerson());
    }

    @Test
    public void test18_addManyAccountsToManyRemotePersons() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            addAccounts(createRemotePerson(j, toString(j), toString(j + SIZE)));
        }
    }

    @Test
    public void test19_addManyAccountsToManyLocalPersons() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            addAccounts(createLocalPerson(j, toString(j), toString(j + SIZE)));
        }
    }

    @Test
    public void test20_getLocalPersonAccountsAddingBeforeCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            addAccounts(createRemotePerson(j, toString(j), toString(j + SIZE)));
            assertEquals(SIZE, getPersonAccountsSize(getLocalPerson(j)));
        }
    }

    @Test
    public void test21_getLocalPersonAccountsAddingAfterCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            final Person remotePerson = createRemotePerson(j, toString(j), toString(j + SIZE));
            final Person localPerson = getLocalPerson(j);
            addAccounts(remotePerson);
            assertEquals(0, getPersonAccountsSize(localPerson));
        }
    }

    @Test
    public void test22_getRemotePersonAccountsAddingBeforeCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            addAccounts(createLocalPerson(j, toString(j), toString(j + SIZE)));
            assertEquals(0, getPersonAccountsSize(getRemotePerson(j)));
        }
    }

    @Test
    public void test23_getRemotePersonAccountsAddingAfterCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            final Person localPerson = createLocalPerson(j, toString(j), toString(j + SIZE));
            final Person remotePerson = getRemotePerson(j);
            addAccounts(localPerson);
            assertEquals(0, getPersonAccountsSize(remotePerson));
        }
    }

    private void addAmount(final Account account) throws RemoteException {
        addAmount(account, GLOBAL_ADDITION);
    }

    private void addAmount(Account account, final long amount) throws RemoteException {
        final long firstAmount = account.getAmount();
        account.addAmount(amount);
        assertEquals(firstAmount + amount, account.getAmount());
    }

    private void checkAmount(final Account account, final long amount) throws RemoteException {
        assertNotNull(account);
        assertEquals(amount, account.getAmount());
    }

    private Account getAccount(final Person person, final String id) throws RemoteException {
        return bank.getAccount(id, person);
    }

    @Test
    public void test24_addAmountToRemotePerson() throws RemoteException {
        final Account remoteAccount = createAccount(createRemotePerson());
        final Account localAccount = getAccount(getLocalPerson(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID);
        checkAmount(remoteAccount, 0);
        addAmount(remoteAccount);
        checkAmount(remoteAccount, GLOBAL_ADDITION);
        checkAmount(localAccount, 0);
    }

    @Test
    public void test25_addAmountToLocalPerson() throws RemoteException {
        final Account localAccount = createAccount(createLocalPerson());
        final Account remoteAccount = getAccount(getRemotePerson(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID);
        checkAmount(localAccount, 0);
        addAmount(localAccount);
        checkAmount(remoteAccount, 0);
        checkAmount(localAccount, GLOBAL_ADDITION);
    }

    @Test
    public void test26_addAmountToRemotePerson() throws RemoteException {
        final Account remoteAccount = createAccount(createRemotePerson());
        final Account remoteAccount2 = getAccount(getRemotePerson(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID);
        checkAmount(remoteAccount, 0);
        addAmount(remoteAccount);
        checkAmount(remoteAccount, GLOBAL_ADDITION);
        checkAmount(remoteAccount2, GLOBAL_ADDITION);
    }

    @Test
    public void test27_addAmountToLocalPerson() throws RemoteException {
        final Account localAccount = createAccount(createLocalPerson());
        final Account localAccount2 = getAccount(getLocalPerson(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID);
        checkAmount(localAccount, 0);
        addAmount(localAccount);
        checkAmount(localAccount, GLOBAL_ADDITION);
        checkAmount(localAccount2, 0);
    }

    @Test
    public void test28_addAmountToLocalAccount() throws RemoteException {
        final Account localAccount = createAccount(createLocalPerson());
        for (int j = 0; j < SIZE; j++) {
            checkAmount(localAccount, GLOBAL_ADDITION * j);
            localAccount.addAmount(GLOBAL_ADDITION);
        }
    }

    @Test
    public void test29_addAmountToRemoteAccount() throws RemoteException {
        final Account remoteAccount = createAccount(createRemotePerson());
        for (int j = 0; j < SIZE; j++) {
            checkAmount(remoteAccount, GLOBAL_ADDITION * j);
            remoteAccount.addAmount(GLOBAL_ADDITION);
        }
    }

    @Test
    public void test30_addAmountToManyLocalAccounts() throws RemoteException {
        List<Account> localAccounts = new ArrayList<>();
        List<Person> localPersons = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            final Person localPerson = (createLocalPerson(i, toString(i), toString(i + SIZE)));
            final Account account = createAccount(localPerson, toString(i));
            localAccounts.add(account);
            for (int j = 0; j < SIZE; j++) {
                checkAmount(account, GLOBAL_ADDITION * j);
                account.addAmount(GLOBAL_ADDITION);
            }
        }
        for (Person person : localPersons) {
            checkAmount(getAccount(person, toString(person.getPassport())), GLOBAL_ADDITION * SIZE);
        }
    }

    @Test
    public void test31_addAmountToManyRemoteAccounts() throws RemoteException {
        final List<Account> remoteAccounts = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            final Account account = createAccount(createRemotePerson(i, toString(i), toString(i + SIZE)), toString(i));
            remoteAccounts.add(account);
            for (int j = 0; j < SIZE; j++) {
                checkAmount(account, GLOBAL_ADDITION * j);
                account.addAmount(GLOBAL_ADDITION);
            }
        }
        for (int i = 0; i < SIZE; i++) {
            checkAmount(getAccount(getRemotePerson(i), toString(i)), GLOBAL_ADDITION * SIZE);
        }
    }

    private Map<String, Account> getAccountMap(final Person person) throws RemoteException {
        return bank.getRemotePerson(person.getPassport()).getPersonAccounts();
    }

    private Runnable getTaskAddAmount(final Account account) {
        return () -> {
            try {
                addAmount(account, GLOBAL_ADDITION);
            } catch (RemoteException e) {
                System.out.println("Can't add amount to account");
            }
        };
    }

    private Runnable getTaskAddAccount(final Person person, final String accountId) {
        return () -> {
            try {
                createAccount(person, accountId);
            } catch (RemoteException e) {
                System.out.println("Can't add amount to account");
            }
        };
    }

    private void checkAccountMap(final Person person, final Map<String, Account> accountMap) throws RemoteException {
        for (Map.Entry<String, Account> entry : accountMap.entrySet()) {
            final Account account = entry.getValue();
            final String accountId = entry.getKey();
            checkAmount(account, GLOBAL_ADDITION * 40);
            checkAccount(account, accountId, person);
        }
    }

    private void checkPersonsList(final List<Person> persons) throws RemoteException {
        for (Person person : persons) {
            checkPersonAccountsSize(person, 30);
            checkAccountMap(person, getAccountMap(person));
        }
    }

    @Test
    public void test32_parallelAddAccounts() throws RemoteException {
        List<Person> localPersons = new ArrayList<>();
        List<Person> remotePersons = new ArrayList<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final Person remotePerson = createRemotePerson(i, toString(i), toString(i + 100));
            final Person localPerson = createLocalPerson(i * 10000 + 100500, toString(i), toString(i + 100));
            remotePersons.add(remotePerson);
            localPersons.add(localPerson);
            for (int j = 0; j < 30; j++) {
                tasks.add(Executors.callable(getTaskAddAccount(remotePerson, toString(j + 200))));
                tasks.add(Executors.callable(getTaskAddAccount(localPerson, toString(j + 200))));
            }
        }
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            System.out.println("Interrupted exception");
            return;
        }
        for (Person person : remotePersons) {
            checkPersonAccountsSize(person, 30);
        }
        for (Person person : localPersons) {
            checkPersonAccountsSize(person, 30);
        }
    }

    @Test
    public void test33_parallelAddAmounts() throws RemoteException {
        List<Person> localPersons = new ArrayList<>();
        List<Person> remotePersons = new ArrayList<>();
        final ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final Person remotePerson = createRemotePerson(i, toString(i), toString(i + 100));
            final Person localPerson = createLocalPerson(i * 10000 + 100500, toString(i), toString(i + 100));
            remotePersons.add(remotePerson);
            localPersons.add(localPerson);
            for (int j = 0; j < 30; j++) {
                final Account remoteAccount = createAccount(remotePerson, toString(j + 200));
                final Account localAccount = createAccount(localPerson, toString(j + 200));
                for (int k = 0; k < 40; k++) {
                    tasks.add(Executors.callable(getTaskAddAmount(remoteAccount)));
                    tasks.add(Executors.callable(getTaskAddAmount(localAccount)));
                }
            }
        }
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            System.out.println("Interrupted exception");
            return;
        }
        checkPersonsList(localPersons);
        checkPersonsList(remotePersons);
    }

    @Test
    public void test34_client() throws RemoteException {
        Client.main(GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME,
                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID, toString(GLOBAL_ADDITION));

        final Person person = getRemotePerson(GLOBAL_PASSPORT);

        checkPerson(person);
        final Account account = getAccount(person, GLOBAL_ACCOUNT_ID);
        checkAccount(account, GLOBAL_ACCOUNT_ID, person);
        checkAmount(account, GLOBAL_ADDITION);

        Client.main(GLOBAL_FIRST_NAME + "incorrect", GLOBAL_LAST_NAME + "incorrect",
                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID, toString(GLOBAL_ADDITION));
        checkPerson(person);
        checkAccount(account, GLOBAL_ACCOUNT_ID, person);
        checkAmount(account, GLOBAL_ADDITION);

        Client.main(GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME,
                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID, toString(GLOBAL_ADDITION));
        checkPerson(person);
        checkAccount(account, GLOBAL_ACCOUNT_ID, person);
        checkAmount(account, 2 * GLOBAL_ADDITION);

        Client.main(GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME,
                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID);
        checkPerson(person);
        checkAccount(account, GLOBAL_ACCOUNT_ID, person);
        checkAmount(account, 2 * GLOBAL_ADDITION);

    }

}
