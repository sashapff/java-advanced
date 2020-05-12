package ru.ifmo.rain.ivanova.bank;

import java.rmi.Remote;

public class RemotePerson extends PersonImpl implements Remote  {
    RemotePerson(int passport, String firstName, String lastName) {
        super(passport, firstName, lastName);
    }
}
