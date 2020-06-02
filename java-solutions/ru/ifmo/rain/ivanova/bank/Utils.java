package ru.ifmo.rain.ivanova.bank;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.rmi.RemoteException;

class Utils {
    protected static String getFullAccountId(final String id, final long password) {
        return password + ":" + id;
    }

    protected static void handleException(final UncheckedIOException e) throws RemoteException {
        final IOException cause = e.getCause();
        if (cause instanceof RemoteException) {
            throw (RemoteException) cause;
        }
        throw e;
    }
}
