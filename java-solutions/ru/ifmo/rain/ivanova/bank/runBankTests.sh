#!/usr/bin/env bash
ROOT="../../../../../../"
LIB="$ROOT/lib"
OUT="$ROOT/out/production/java-advanced-2020-solutions"
java -cp "$OUT":"$LIB/junit-4.11.jar":"$LIB/hamcrest-core-1.3.jar" ru.ifmo.rain.ivanova.bank.BankTests

