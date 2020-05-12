package ru.ifmo.rain.ivanova.bank;

class PersonImpl implements Person {
    private final int passport;
    private final String firstName;
    private final String lastName;

    PersonImpl(int passport, String firstName, String lastName) {
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
