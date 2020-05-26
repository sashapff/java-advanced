package ru.ifmo.rain.ivanova.bank;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class BankTest {
    private static final int PORT = 4040;
    private static Bank bank;
    private static Registry registry;
    private static final int GLOBAL_PASSPORT = 0;
    private static final String GLOBAL_FIRST_NAME = "First";
    private static final String GLOBAL_LAST_NAME = "Last";
    private static final String GLOBAL_ACCOUNT_ID = "Account";
    private static final int GLOBAL_ADDITION = 50;
    private static final int SIZE = 10;

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

    private void checkPerson(final Person person, final int passport,
                             final String firstName, final String lastName) throws RemoteException {
        assertNotNull(person);
        assertEquals(firstName, person.getFirstName());
        assertEquals(lastName, person.getLastName());
        assertEquals(passport, person.getPassport());
    }

    private Person createPerson() throws RemoteException {
        return createPerson(GLOBAL_PASSPORT, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
    }

    private Person createPerson(final int passport, final String firstName,
                                final String lastName) throws RemoteException {
        final Person person = bank.createPerson(passport, firstName, lastName);
        checkPerson(person, passport, firstName, lastName);
        return person;
    }

    @Test
    public void test00_createPerson() throws RemoteException {
        createPerson();
    }

    private Person createRemotePerson() throws RemoteException {
        return createRemotePerson(GLOBAL_PASSPORT, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
    }

    private Person createRemotePerson(final int passport, final String firstName,
                                      final String lastName) throws RemoteException {
        createPerson(passport, firstName, lastName);
        final Person remotePerson = bank.getRemotePerson(passport);
        checkPerson(remotePerson, passport, firstName, lastName);
        return remotePerson;
    }

    private Person createLocalPerson() throws RemoteException {
        return createLocalPerson(GLOBAL_PASSPORT, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
    }

    private Person createLocalPerson(final int passport, final String firstName,
                                     final String lastName) throws RemoteException {
        createPerson(passport, firstName, lastName);
        final Person localPerson = bank.getLocalPerson(passport);
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
        final Person person = createPerson();
        final Person samePerson = createPerson();
        checkPersons(person, samePerson);
    }

    @Test
    public void test04_getNotExistingPerson() throws RemoteException {
        assertNull(bank.getRemotePerson(GLOBAL_PASSPORT));
        assertNull(bank.getRemotePerson(GLOBAL_PASSPORT));
    }

    private String toString(final int i) {
        return Integer.toString(i);
    }

    private Person getRemotePerson(final int passport) throws RemoteException {
        return bank.getRemotePerson(passport);
    }

    private Person getLocalPerson(final int passport) throws RemoteException {
        return bank.getLocalPerson(passport);
    }

    @Test
    public void test05_createAndGetManyPersons() throws RemoteException {
        for (int i = 0; i < SIZE; i++) {
            final int passport = i;
            final String firstName = toString(i);
            final String lastName = toString(i + SIZE);
            createRemotePerson(passport, firstName, lastName);
            createLocalPerson(passport, firstName, lastName);
        }
        for (int i = 0; i < SIZE; i++) {
            final int passport = i;
            final String firstName = toString(i);
            final String lastName = toString(i + SIZE);
            checkPerson(getRemotePerson(passport), passport, firstName, lastName);
            checkPerson(getLocalPerson(passport), passport, firstName, lastName);
        }
    }

    private String getFullAccountId(final String id, final int passport) {
        return passport + ":" + id;
    }

    private void checkAccount(final Account account, final String accountId,
                              final Person person) throws RemoteException {
        final String fullAccountId = getFullAccountId(accountId, person.getPassport());
        assertNotNull(account);
        assertEquals(fullAccountId, account.getId());
        Account personAccount = bank.getAccount(accountId, person);
        assertNotNull(personAccount);
        assertEquals(account.getId(), personAccount.getId());
        assertEquals(account.getAmount(), personAccount.getAmount());
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
        createAccount(createPerson());
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
        createAccountTwice(createPerson());
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
        assertNull(bank.getAccount("100500", createPerson()));
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
        final Person person = createPerson();
        addAccounts(person);
        assertEquals(SIZE, getPersonAccountsSize(person));
        addAccounts(person, SIZE);
    }

    private void checkPersonAccountsSize(final Person person, final int size) throws RemoteException {
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
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + SIZE);
            addAccounts(createRemotePerson(passport, firstName, lastName));
        }
    }

    @Test
    public void test19_addManyAccountsToManyLocalPersons() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + SIZE);
            addAccounts(createLocalPerson(passport, firstName, lastName));
        }
    }

    @Test
    public void test20_getLocalPersonAccountsAddingBeforeCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + SIZE);
            addAccounts(createRemotePerson(passport, firstName, lastName));
            assertEquals(SIZE, createLocalPerson(passport, firstName, lastName)
                    .getPersonAccounts().size());
        }
    }

    @Test
    public void test21_getLocalPersonAccountsAddingAfterCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + SIZE);
            final Person remotePerson = createRemotePerson(passport, firstName, lastName);
            final Person localPerson = createLocalPerson(passport, firstName, lastName);
            addAccounts(remotePerson);
            assertEquals(0, localPerson.getPersonAccounts().size());
        }
    }

    @Test
    public void test22_getRemotePersonAccountsAddingBeforeCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + SIZE);
            addAccounts(createLocalPerson(passport, firstName, lastName));
            assertEquals(0, createRemotePerson(passport, firstName, lastName)
                    .getPersonAccounts().size());
        }
    }

    @Test
    public void test23_getRemotePersonAccountsAddingAfterCreating() throws RemoteException {
        for (int j = 0; j < SIZE; j++) {
            final int passport = j;
            final String firstName = toString(j);
            final String lastName = toString(j + SIZE);
            final Person localPerson = createLocalPerson(passport, firstName, lastName);
            final Person remotePerson = createRemotePerson(passport, firstName, lastName);
            addAccounts(localPerson);
            assertEquals(0, remotePerson.getPersonAccounts().size());
        }
    }

    private void addAmount(final Account account) throws RemoteException {
        addAmount(account, GLOBAL_ADDITION);
    }

    private void addAmount(Account account, final int amount) throws RemoteException {
        final int firstAmount = account.getAmount();
        account.addAmount(amount);
        assertEquals(firstAmount + amount, account.getAmount());
    }

    void checkAmount(final Person person, final int amount) throws RemoteException {
        assertEquals(amount, bank.getAccount(GLOBAL_ACCOUNT_ID, person).getAmount());
    }

    @Test
    public void test24_ChangeAmount() throws RemoteException {
        Account account = createAccount(createPerson());
        Person beforeLocalPerson = createLocalPerson();
        Person beforeRemotePerson = createRemotePerson();
        addAmount(account);
        Person afterLocalPerson = createLocalPerson();
        Person afterRemotePerson = createRemotePerson();
        checkPersonAccountsSize(beforeLocalPerson, 0);
        checkPersonAccountsSize(beforeRemotePerson, GLOBAL_ADDITION);
        checkPersonAccountsSize(afterLocalPerson, GLOBAL_ADDITION);
        checkPersonAccountsSize(afterRemotePerson, GLOBAL_ADDITION);
    }

    @Test
    public void test25_() throws RemoteException {
        Person beforeLocalPerson = createLocalPerson();
        Person beforeRemotePerson = createRemotePerson();
        addAmount(createAccount(beforeLocalPerson));
        Person afterLocalPerson = createLocalPerson();
        Person afterRemotePerson = createRemotePerson();
        checkPersonAccountsSize(beforeLocalPerson, 0);
        checkPersonAccountsSize(beforeRemotePerson, GLOBAL_ADDITION);
        checkPersonAccountsSize(afterLocalPerson, GLOBAL_ADDITION);
        checkPersonAccountsSize(afterRemotePerson, GLOBAL_ADDITION);
    }

    @Test
    public void test25_addRemotePersonAmountAfterCreatingLocalPersonAccount() throws RemoteException {
        final Person remotePerson = createRemotePerson();
        final Person localPerson = createLocalPerson();
        final Account remoteAccount = createAccount(remotePerson);
        final Account localAccount = createAccount(localPerson);
        addAmount(remoteAccount);
        assertEquals(GLOBAL_ADDITION, localAccount.getAmount());
    }

    Account getAccount(Person person) throws RemoteException {
        return getAccount(person, GLOBAL_ACCOUNT_ID);
    }

    Account getAccount(Person person, String accountId) throws RemoteException {
        Account account = bank.getAccount(accountId, person);
//        checkAccount(account, person);
        return account;
    }

//    @Test
//    public void test24_setAmountInRemoteAccountCreatingRemoteAccountBeforeLocalPerson() throws RemoteException {
//        final Person remotePerson = createRemotePerson();
//        final Account remoteAccount = createAccount(remotePerson);
//        Person localPerson = createLocalPerson();
//        remoteAccount.addAmount(GLOBAL_ADDITION);
//        checkAccount(remoteAccount, remotePerson, GLOBAL_ACCOUNT_ID, GLOBAL_PASSPORT, GLOBAL_ADDITION);
//        Account localAccount = getAccount(localPerson);
//        assertEquals(0, localAccount.getAmount());
//        localPerson = bank.getLocalPerson(GLOBAL_PASSPORT);
//        localAccount = bank.getAccount(GLOBAL_ACCOUNT_ID, localPerson);
//        assertEquals(GLOBAL_ADDITION, localAccount.getAmount());
//    }
//
//    @Test
//    public void test25_setAmountOfLocalAccountCreatingRemoteAccountAfterLocalPerson() throws RemoteException {
//        final Person localPerson = createLocalPerson();
//        Account localAccount = bank.createAccount(GLOBAL_ACCOUNT_ID, localPerson);
//        assertNotNull(localAccount);
//        localAccount.addAmount(GLOBAL_ADDITION);
//        assertEquals(GLOBAL_ADDITION, localAccount.getAmount());
//        localAccount = bank.getAccount(GLOBAL_ACCOUNT_ID, localPerson);
//        assertNotNull(localAccount);
//        assertEquals(GLOBAL_ADDITION, localAccount.getAmount());
//        final Person remotePerson = createRemotePerson();
//        final Account remoteAccount = bank.getAccount(GLOBAL_ACCOUNT_ID, remotePerson);
//        assertNotNull(remoteAccount);
//        assertEquals(0, remoteAccount.getAmount());
//    }
//
//    @Test
//    public void test26_manyAccountsOfOneRemotePerson() throws RemoteException {
//        final Person person = bank.createPerson(GLOBAL_PASSPORT, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
//        assertNotNull(person);
//        final List<Person> persons = new ArrayList<>();
//        for (int i = 0; i < SIZE; i++) {
//            persons.add(bank.getRemotePerson(GLOBAL_PASSPORT));
//        }
//        for (int i = 0; i < SIZE; i++) {
//            for (int j = 0; j < SIZE; j++) {
//                assertNotNull(bank.createAccount(i + "+" + j, persons.get(j)));
//            }
//        }
//        for (int i = 0; i < SIZE; i++) {
//            assertEquals(SIZE * SIZE, persons.get(i).getPersonAccounts().size());
//        }
//    }
//
//    @Test
//    public void test27_setAmountOfManyAccountsOfManyPersons() throws RemoteException {
//        final List<Person> persons = new ArrayList<>();
//        for (int i = 0; i < SIZE; i++) {
//            final Person person = bank.createPerson(i, GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME);
//            assertNotNull(person);
//            persons.add(bank.getRemotePerson(i));
//        }
//        final Map<String, Integer> answer = new HashMap<>();
//        for (int i = 0; i < SIZE; i++) {
//            for (int j = 0; j < SIZE; j++) {
//                final String accountId = i + "+" + j;
//                final Account account = bank.createAccount(accountId, persons.get(j));
//                assertNotNull(account);
//                account.addAmount(i + j);
//                answer.put(accountId, i + j);
//            }
//        }
//        for (int i = 0; i < SIZE; i++) {
//            for (int j = 0; j < SIZE; j++) {
//                final String accountId = i + "+" + j;
//                assertEquals((int) answer.get(accountId), bank.getAccount(accountId, persons.get(j)).getAmount());
//            }
//        }
//    }
//
//    @Test
//    public void test28_setAmountTwice() throws RemoteException {
//        for (var i = 0; i < SIZE; ++i) {
//            final Person person = bank.createPerson(GLOBAL_PASSPORT + i,
//                    GLOBAL_FIRST_NAME + i, GLOBAL_LAST_NAME + i);
//            assertNotNull(person);
//        }
//
//        for (var i = 0; i < SIZE; i++) {
//            final Person remotePerson = bank.getRemotePerson(GLOBAL_PASSPORT + i);
//            for (var j = 0; j < SIZE; j++) {
//                bank.createAccount("Account" + j, remotePerson);
//            }
//        }
//
//        for (var i = 0; i < SIZE; i++) {
//            final Person person = bank.getRemotePerson(GLOBAL_PASSPORT + i);
//            for (var j = 0; j < SIZE; j++) {
//                final Account account = bank.getAccount("Account" + j, person);
//                assertNotNull(account);
//                account.addAmount(account.getAmount() + GLOBAL_ADDITION);
//                assertEquals(GLOBAL_ADDITION, account.getAmount());
//            }
//        }
//
//        for (var i = 0; i < SIZE; i++) {
//            final Person person = bank.getRemotePerson(GLOBAL_PASSPORT + i);
//            for (var j = 0; j < SIZE; j++) {
//                final Account account = bank.getAccount("Account" + j, person);
//                assertNotNull(account);
//                account.addAmount(-GLOBAL_ADDITION);
//                assertEquals(0, account.getAmount());
//            }
//        }
//    }
//
//    @Test
//    public void test29_client() throws RemoteException {
//        Client.main(GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME,
//                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID, toString(GLOBAL_ADDITION));
//        final Person person = bank.getRemotePerson(GLOBAL_PASSPORT);
//        checkPerson(person);
//        final Account account = bank.getAccount(GLOBAL_ACCOUNT_ID, person);
//        checkAccount(account, person, GLOBAL_ACCOUNT_ID, GLOBAL_PASSPORT, GLOBAL_ADDITION);
//
//        Client.main(GLOBAL_FIRST_NAME + "incorrect", GLOBAL_LAST_NAME + "incorrect",
//                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID, toString(GLOBAL_ADDITION));
//        checkPerson(person);
//        checkAccount(account, person, GLOBAL_ACCOUNT_ID, GLOBAL_PASSPORT, GLOBAL_ADDITION);
//
//        Client.main(GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME,
//                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID, toString(GLOBAL_ADDITION));
//        checkPerson(person);
//        checkAccount(account, person, GLOBAL_ACCOUNT_ID, GLOBAL_PASSPORT, 2 * GLOBAL_ADDITION);
//
//        Client.main(GLOBAL_FIRST_NAME, GLOBAL_LAST_NAME,
//                toString(GLOBAL_PASSPORT), GLOBAL_ACCOUNT_ID);
//        checkPerson(person);
//        checkAccount(account, person, GLOBAL_ACCOUNT_ID, GLOBAL_PASSPORT, 2 * GLOBAL_ADDITION);
//
//    }
//
}
