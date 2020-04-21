package ru.ifmo.rain.ivanova.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Implementor implements Impler {
    public static void main(String[] args) throws ImplerException {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            throw new ImplerException("Invalid arguments");
        }
        try {
            new Implementor().implement(Class.forName(args[0]), Path.of(args[1]));
        } catch (ClassNotFoundException e) {
            throw new ImplerException("Not found class: " + e.getMessage());
        } catch (InvalidPathException e) {
            throw new ImplerException("Invalid path: " + e.getMessage());
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
//        if (token == null || root == null) {
//            throw new ImplerException("Arguments are null");
//        }
//        if (token.isPrimitive() || token.isArray() || token == Enum.class || Modifier.isFinal(token.getModifiers())) {
//            throw new ImplerException("Error in token");
//        }
//        Path sourceCodeFile = root.resolve(token.getPackageName().replace('.', File.separatorChar))
//                .resolve(token.getSimpleName() + "Impl.java");
//        if (sourceCodeFile.getParent() != null) {
//            try {
//                Files.createDirectories(sourceCodeFile.getParent());
//            } catch (IOException e) {
//                throw new ImplerException("Error in creating directories: " + e.getMessage());
//            }
//        }
//        try (BufferedWriter writer = Files.newBufferedWriter(sourceCodeFile)) {
//            writer.write(ImplementorUtils.generateSourceCode(token));
//        } catch (IOException e) {
//            throw new ImplerException("Error in writing", e);
//        }
    }
}
