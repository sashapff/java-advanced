//package ru.ifmo.rain.ivanova.bank;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//abstract class AbstractPerson implements Person {
//<<<<<<< HEAD
//    private final int passport;
//    private final String firstName;
//    private final String lastName;
//    private final ConcurrentHashMap<String, Account> accounts;
//
//    AbstractPerson(final int passport, final String firstName, final String lastName,
//                   final ConcurrentHashMap<String, Account> accounts) {
//=======
//    // :NOTE: Модификаторы доступа
//    final int passport;
//    final String firstName;
//    final String lastName;
//    final ConcurrentHashMap<String, Account> accounts;
//
//
//    AbstractPerson(final int passport, final String firstName, final String lastName, final ConcurrentHashMap<String, Account> accounts) {
//        // :NOTE: Копипаста
//>>>>>>> 50b2be5fda4ec93ddf7c68c8b97362a6f515902f
//        this.passport = passport;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.accounts = accounts;
//    }
//
//    AbstractPerson(final int passport, final String firstName, final String lastName) {
//<<<<<<< HEAD
//        this(passport, firstName, lastName, new ConcurrentHashMap<>());
//=======
//        this.passport = passport;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.accounts = new ConcurrentHashMap<>();
//>>>>>>> 50b2be5fda4ec93ddf7c68c8b97362a6f515902f
//    }
//
//    @Override
//    public int getPassport() {
//        return passport;
//    }
//
//    @Override
//    public String getFirstName() {
//        return firstName;
//    }
//
//    @Override
//    public String getLastName() {
//        return lastName;
//    }
//
//    @Override
//    public void addAccount(final String id, final Account account) {
//<<<<<<< HEAD
//        accounts.put(id, account);
//=======
//        accounts.putIfAbsent(id, account);
//>>>>>>> 50b2be5fda4ec93ddf7c68c8b97362a6f515902f
//    }
//
//    @Override
//    public Account getAccount(final String id) {
//        return accounts.get(id);
//    }
//
//    @Override
//    public Map<String, Account> getPersonAccounts() {
//        return Map.copyOf(accounts);
//    }
//
//}
