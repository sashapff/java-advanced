//package ru.ifmo.rain.ivanova.bank;
//
//import org.junit.internal.TextListener;
//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;
//
//public class BankTests {
//    public static void main(String[] args) {
//        JUnitCore junit = new JUnitCore();
//        junit.addListener(new TextListener(System.out));
//
//        Result result = junit.run(BankTest.class);
//
//        if (result.getFailureCount() > 0) {
//            System.exit(1);
//        }
//        System.exit(0);
//    }
//
//}