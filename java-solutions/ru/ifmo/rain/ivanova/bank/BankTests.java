package ru.ifmo.rain.ivanova.bank;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class BankTests {
    public static void main(final String[] args) {
        final JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));

        final Result result = junit.run(BankTest.class);

        System.exit(result.getFailureCount() > 0 ? 1 : 0);
    }

}