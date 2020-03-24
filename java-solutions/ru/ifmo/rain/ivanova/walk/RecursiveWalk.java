package ru.ifmo.rain.ivanova.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class RecursiveWalk {

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Enter input and output files");
            return;
        }
        try {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]))) {
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[1]))) {
                    try {
                        HashFileVisitor hashFileVisitor = new HashFileVisitor(writer);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            try {
                                Files.walkFileTree(Paths.get(line), hashFileVisitor);
                            } catch (InvalidPathException e) {
                                hashFileVisitor.print(0, line);
                            } catch (IOException e) {
                                throw new WalkException("Error in writing in output file", e);
                            }
                        }
                    } catch (IOException e) {
                        throw new WalkException("Error in reading input file", e);
                    }
                } catch (IOException e) {
                    throw new WalkException("Not found output file", e);
                } catch (InvalidPathException e) {
                    throw new WalkException("Invalid name of output file", e);
                }
            } catch (IOException e) {
                throw new WalkException("Not found input file", e);
            } catch (InvalidPathException e) {
                throw new WalkException("Invalid name of input file", e);
            }
        } catch (WalkException e) {
            System.err.println(e.getMessage());
        }
    }
}