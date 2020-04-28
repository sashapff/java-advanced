package ru.ifmo.rain.ivanova.bank;

public class RemotePerson implements Person {
    private int passport;
    private String firstName;
    private String lastName;

    public RemotePerson(int passport, String firstName, String lastName) {
        this.passport = passport;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public int getPassport() {
        return passport;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }
}
