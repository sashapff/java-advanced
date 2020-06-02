package ru.ifmo.rain.ivanova.bank;

import java.util.concurrent.ConcurrentHashMap;

class RemotePerson extends AbstractPerson {

    RemotePerson(final long passport, final String firstName, final String lastName) {
        super(passport, firstName, lastName, new ConcurrentHashMap<>());
    }

}
