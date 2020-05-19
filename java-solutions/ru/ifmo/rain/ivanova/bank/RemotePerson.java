package ru.ifmo.rain.ivanova.bank;

import java.util.concurrent.ConcurrentHashMap;

class RemotePerson extends AbstractPerson {

    RemotePerson(int passport, String firstName, String lastName) {
        super(passport, firstName, lastName, new ConcurrentHashMap<>());
    }

}
