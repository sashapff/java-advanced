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

import static junit.framework.TestCase.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankTest {
    private static final int PORT = 4040;
    private static Bank bank;
    private static Registry registry;
    private final int globalPassport = 0;
    private final String globalFirstName = "First";
    private final String globalLastName = "Last";
    private final String globalAccountId = "Account";
    private final int globalAddition = 50;

    @BeforeClass
    public static void beforeClass() throws RemoteException {
        registry = LocateRegistry.createRegistry(PORT);
    }

    @Before
    public void before() throws RemoteException {
        bank = new RemoteBank(PORT);
        Bank stub = (Bank) UnicastRemoteObject.exportObject(bank, 0);
        registry.rebind("bank", stub);
        System.out.println("New test!");
    }

    @Test
    public void test00_getPersonInfo() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        assertEquals(globalPassport, person.getPassport());
        assertEquals(globalFirstName, person.getFirstName());
        assertEquals(globalLastName, person.getLastName());
    }

    @Test
    public void test01_getRemotePersonInfo() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertNotNull(remotePerson);
        assertEquals(globalPassport, remotePerson.getPassport());
        assertEquals(globalFirstName, remotePerson.getFirstName());
        assertEquals(globalLastName, remotePerson.getLastName());
    }

    @Test
    public void test02_getLocalPersonInfo() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        assertEquals(globalPassport, localPerson.getPassport());
        assertEquals(globalFirstName, localPerson.getFirstName());
        assertEquals(globalLastName, localPerson.getLastName());
    }

    @Test
    public void test03_createPersonTwice() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        assertEquals(person, bank.createPerson(globalPassport, globalFirstName, globalLastName));
    }

    @Test
    public void test04_getPersonWithoutCreating() throws RemoteException {
        assertNull(bank.getRemotePerson(100500));
    }

    private String toString(final int i) {
        return Integer.toString(i);
    }

    @Test
    public void test05_getManyPersonsInfo() throws RemoteException {
        for (int i = 0; i < 100; i++) {
            final int passport = i;
            final String firstName = toString(i);
            final String lastName = toString(i);
            Person person = bank.createPerson(passport, firstName, lastName);
            assertNotNull(person);

            Person remotePerson = bank.getRemotePerson(passport);
            assertNotNull(remotePerson);
            assertEquals(firstName, remotePerson.getFirstName());
            assertEquals(lastName, remotePerson.getLastName());
            assertEquals(passport, remotePerson.getPassport());

            Person localPerson = bank.getLocalPerson(passport);
            assertNotNull(localPerson);
            assertEquals(firstName, localPerson.getFirstName());
            assertEquals(lastName, localPerson.getLastName());
            assertEquals(passport, localPerson.getPassport());
        }
    }

    private String getFullAccountId(final String id, final int password) {
        return password + ":" + id;
    }

    @Test
    public void test06_getAccountInfo() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Account account = bank.createAccount(globalAccountId, person);
        assertNotNull(account);
        assertEquals(account, bank.getAccount(globalAccountId, person));
        assertEquals(getFullAccountId(globalAccountId, globalPassport), account.getId());
        assertEquals(0, account.getAmount());
    }

    @Test
    public void test07_getAccountInfoOfRemotePerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        Account account = bank.createAccount(globalAccountId, remotePerson);
        assertNotNull(account);
        assertEquals(account, bank.getAccount(globalAccountId, remotePerson));
        assertEquals(getFullAccountId(globalAccountId, globalPassport), account.getId());
        assertEquals(0, account.getAmount());
    }

    @Test
    public void test08_getAccountInfoOfLocalPerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person localPerson = bank.getLocalPerson(globalPassport);
        Account account = bank.createAccount(globalAccountId, localPerson);
        assertNotNull(account);
        assertEquals(account, bank.getAccount(globalAccountId, localPerson));
        assertEquals(globalAccountId, account.getId());
        assertEquals(0, account.getAmount());
    }

    @Test
    public void test09_createAccountTwice() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Account account = bank.createAccount(globalAccountId, person);
        assertEquals(account, bank.createAccount(globalAccountId, person));
    }

    @Test
    public void test10_createAccountOfRemotePersonTwice() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertNotNull(remotePerson);
        Account account = bank.createAccount(globalAccountId, remotePerson);
        assertEquals(account, bank.createAccount(globalAccountId, remotePerson));
    }

    @Test
    public void test11_createAccountOfLocalPersonTwice() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        Account account = bank.createAccount(globalAccountId, localPerson);
        assertEquals(account, bank.createAccount(globalAccountId, localPerson));
    }

    @Test
    public void test12_getAccountWithoutCreating() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        assertNull(bank.getAccount(toString(100500), person));
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertNotNull(remotePerson);
        assertNull(bank.getAccount(toString(100500), remotePerson));
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        assertNull(bank.getAccount(toString(100500), localPerson));
    }

    @Test
    public void test13_createManyAccountsOfOnePerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        for (int i = 0; i < 100; i++) {
            final String accountId = toString(i);
            Account account = bank.createAccount(accountId, person);
            assertEquals(account, bank.getAccount(accountId, person));
            assertEquals(getFullAccountId(accountId, globalPassport), account.getId());
            assertEquals(0, account.getAmount());
        }
    }

    @Test
    public void test14_addAccountToRemotePerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertEquals(0, bank.getAccounts(remotePerson).size());
        bank.createAccount(globalAccountId, remotePerson);
        assertEquals(1, bank.getAccounts(remotePerson).size());
    }

    @Test
    public void test15_addAccountToLocalPerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertEquals(0, bank.getAccounts(localPerson).size());
        bank.createAccount(globalAccountId, localPerson);
        assertEquals(1, bank.getAccounts(localPerson).size());
    }

    @Test
    public void test16_addManyAccountsToRemotePerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertNotNull(remotePerson);
        for (int i = 0; i < 100; i++) {
            assertEquals(i, bank.getAccounts(remotePerson).size());
            bank.createAccount(toString(i), remotePerson);
        }
    }

    @Test
    public void test17_addManyAccountsToLocalPerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        for (int i = 0; i < 100; i++) {
            assertEquals(i, bank.getAccounts(localPerson).size());
            bank.createAccount(toString(i), localPerson);
        }
    }

    @Test
    public void test18_addManyAccountsToManyRemotePersons() throws RemoteException {
        for (int j = 0; j < 100; j++) {
            Person person = bank.createPerson(j, toString(j), toString(j));
            assertNotNull(person);
            Person remotePerson = bank.getRemotePerson(j);
            assertNotNull(remotePerson);
            for (int i = 0; i < 100; i++) {
                assertEquals(i, bank.getAccounts(remotePerson).size());
                bank.createAccount(toString(i), remotePerson);
            }
        }
    }

    @Test
    public void test19_addManyAccountsToManyLocalPersons() throws RemoteException {
        for (int j = 0; j < 100; j++) {
            Person person = bank.createPerson(j, toString(j), toString(j));
            assertNotNull(person);
            Person localPerson = bank.getLocalPerson(j);
            assertNotNull(localPerson);
            for (int i = 0; i < 100; i++) {
                assertEquals(i, bank.getAccounts(localPerson).size());
                bank.createAccount(toString(i), localPerson);
            }
        }
    }

    @Test
    public void test20_getAccountsOfLocalPersonAfterCreating() throws RemoteException {
        for (int j = 0; j < 100; j++) {
            Person person = bank.createPerson(j, toString(j), toString(j));
            assertNotNull(person);
            Person remotePerson = bank.getRemotePerson(j);
            assertNotNull(remotePerson);
            for (int i = 0; i < 100; i++) {
                assertEquals(i, bank.getAccounts(remotePerson).size());
                bank.createAccount(toString(i), remotePerson);
            }
            Person localPerson = bank.getLocalPerson(j);
            assertNotNull(localPerson);
            assertEquals(100, bank.getAccounts(localPerson).size());
        }
    }

    @Test
    public void test21_getAccountsOfLocalPersonBeforeCreating() throws RemoteException {
        for (int j = 0; j < 100; j++) {
            Person person = bank.createPerson(j, toString(j), toString(j));
            assertNotNull(person);
            Person remotePerson = bank.getRemotePerson(j);
            assertNotNull(remotePerson);
            Person localPerson = bank.getLocalPerson(j);
            assertNotNull(localPerson);
            for (int i = 0; i < 100; i++) {
                assertEquals(bank.getAccounts(remotePerson).size(), i);
                bank.createAccount(toString(i), remotePerson);
            }
            assertEquals(bank.getAccounts(localPerson).size(), 0);
        }
    }

    @Test
    public void test22_setAmountInAccount() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertNotNull(remotePerson);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        Account localAccount = bank.createAccount(globalAccountId, localPerson);
        Account remoteAccount = bank.getAccount(globalAccountId, remotePerson);
        assertNotNull(localAccount);
        assertNotNull(remoteAccount);
        localAccount.setAmount(globalAddition);
        assertEquals(globalAddition, localAccount.getAmount());
        assertEquals(0, remoteAccount.getAmount());
    }

    @Test
    public void test23_setAmountInAccount() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertNotNull(remotePerson);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        assertTrue(localPerson instanceof LocalPerson);
        assertTrue(remotePerson instanceof RemotePerson);
        Account remoteAccount = bank.createAccount(globalAccountId, remotePerson);
        Account localAccount = bank.getAccount(globalAccountId, localPerson);
        assertNotNull(remoteAccount);
        assertNotNull(localAccount);
        assertEquals(localAccount, remoteAccount);
        remoteAccount.setAmount(globalAddition);
        assertEquals(globalAddition, remoteAccount.getAmount());
        assertEquals(globalAddition, localAccount.getAmount());
    }

    @Test
    public void test24_setAmountOfAccount() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        assertNotNull(remotePerson);
        Account remoteAccount = bank.createAccount(globalAccountId, remotePerson);
        assertNotNull(remoteAccount);
        Account localAccount = bank.getAccount(globalAccountId, localPerson);
        assertNotNull(localAccount);
        localPerson = bank.getLocalPerson(globalPassport);
        remoteAccount.setAmount(globalAddition);
        assertEquals(getFullAccountId(globalAccountId, remotePerson.getPassport()), remoteAccount.getId());
        assertEquals(globalAddition, remoteAccount.getAmount());
        localAccount = bank.getAccount(globalAccountId, localPerson);
        assertEquals(0, localAccount.getAmount());
        localPerson = bank.getLocalPerson(globalPassport);
        localAccount = bank.getAccount(globalAccountId, localPerson);
        assertEquals(globalAddition, localAccount.getAmount());
    }

    @Test
    public void test25_setAmountOfAccount() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        Person localPerson = bank.getLocalPerson(globalPassport);
        assertNotNull(localPerson);
        Account localAccount = bank.createAccount(globalAccountId, localPerson);
        localAccount.setAmount(globalAddition);
        localAccount = bank.getAccount(globalAccountId, localPerson);
        assertEquals(localAccount.getAmount(), globalAddition);
        Person remotePerson = bank.getRemotePerson(globalPassport);
        Account remoteAccount = bank.getAccount(globalAccountId, remotePerson);
        assertEquals(remoteAccount.getAmount(), 0);
    }

    @Test
    public void test26_manyAccountsOfOneRemotePerson() throws RemoteException {
        Person person = bank.createPerson(globalPassport, globalFirstName, globalLastName);
        assertNotNull(person);
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            persons.add(bank.getRemotePerson(globalPassport));
        }
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                assertNotNull(bank.createAccount(i + "+" + j, persons.get(j)));
            }
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(10000, bank.getAccounts(persons.get(i)).size());
        }
    }

    @Test
    public void test27_setAmountOfManyAccountsOfManyPersons() throws RemoteException {
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Person person = bank.createPerson(i, globalFirstName, globalLastName);
            assertNotNull(person);
            persons.add(bank.getRemotePerson(i));
        }
        Map<String, Integer> answer = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                final String accountId = i + "+" + j;
                Account account = bank.createAccount(accountId, persons.get(j));
                assertNotNull(account);
                account.setAmount(i + j);
                answer.put(accountId, i + j);
            }
        }
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                final String accountId = i + "+" + j;
                assertEquals((int) answer.get(accountId), bank.getAccount(accountId, persons.get(j)).getAmount());
            }
        }
    }

    @Test
    public void test28_setAmountTwice() throws RemoteException {
        for (var i = 0; i < 100; ++i) {
            Person person = bank.createPerson(globalPassport + i,
                    globalFirstName + i, globalLastName + i);
            assertNotNull(person);
        }

        for (var i = 0; i < 100; i++) {
            Person person = bank.getRemotePerson(globalPassport + i);
            for (var j = 0; j < 100; j++) {
                bank.createAccount("Account" + j, person);
            }
        }

        for (var i = 0; i < 100; i++) {
            Person person = bank.getRemotePerson(globalPassport + i);
            for (var j = 0; j < 100; j++) {
                Account account = bank.getAccount("Account" + j, person);
                assertNotNull(account);
                account.setAmount(account.getAmount() + 50);
                assertEquals(50, account.getAmount());
            }
        }

        for (var i = 0; i < 100; i++) {
            Person person = bank.getRemotePerson(globalPassport + i);
            for (var j = 0; j < 100; j++) {
                Account account = bank.getAccount("Account" + j, person);
                assertNotNull(account);
                account.setAmount(account.getAmount() - 50);
                assertEquals(0, account.getAmount());
            }
        }
    }

    @Test
    public void test29_client() throws RemoteException {
        Client.main(globalFirstName, globalLastName,
                toString(globalPassport), globalAccountId, toString(globalAddition));
        Person person = bank.getRemotePerson(globalPassport);
        assertNotNull(person);
        assertEquals(globalPassport, person.getPassport());
        assertEquals(globalFirstName, person.getFirstName());
        assertEquals(globalLastName, person.getLastName());
        Account account = bank.getAccount(globalAccountId, person);
        assertNotNull(account);
        assertEquals(getFullAccountId(globalAccountId, person.getPassport()), account.getId());
        assertEquals(globalAddition, account.getAmount());

        Client.main(globalFirstName + "incorrect", globalLastName + "incorrect",
                toString(globalPassport), globalAccountId, toString(globalAddition));
        assertEquals(globalPassport, person.getPassport());
        assertEquals(globalFirstName, person.getFirstName());
        assertEquals(globalLastName, person.getLastName());
        assertEquals(getFullAccountId(globalAccountId, person.getPassport()), account.getId());
        assertEquals(globalAddition, account.getAmount());

        Client.main(globalFirstName, globalLastName,
                toString(globalPassport), globalAccountId, toString(globalAddition));
        assertEquals(globalPassport, person.getPassport());
        assertEquals(globalFirstName, person.getFirstName());
        assertEquals(globalLastName, person.getLastName());
        assertEquals(getFullAccountId(globalAccountId, person.getPassport()), account.getId());
        assertEquals(2 * globalAddition, account.getAmount());

        Client.main(globalFirstName, globalLastName,
                toString(globalPassport), globalAccountId);
    }

}