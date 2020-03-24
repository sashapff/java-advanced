package ru.ifmo.rain.ivanova.walk;

class WalkException extends Exception {
    WalkException(String message, Throwable e) {
        super(message + ": " + e.getMessage(), e);
    }
}