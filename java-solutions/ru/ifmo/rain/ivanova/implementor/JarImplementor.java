package ru.ifmo.rain.ivanova.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class implementing {@link Impler} and {@link JarImpler}. Provide public methods to create {@code .java}
 * and {@code .jar} implementing or extending given class or interface.
 *
 * @author sasha.pff
 * @version 1.0
 * @see Impler
 * @see JarImpler
 */
public class JarImplementor implements Impler, JarImpler {

    /**
     * Main function. Provides console interface for {@link JarImplementor}. Supports two modes.
     * <ol>
     * <li> 2 arguments {@code className outputPath} creates {@code .java} file by executing
     * provided with {@link Impler} method {@link #implement(Class, Path)} </li>
     * <li> 3 arguments {@code -jar className jarOutputPath} creates {@code .jar} file by executing
     * provided with {@link JarImpler} method {@link #implementJar(Class, Path)} </li>
     * </ol>
     *
     * @param args command line arguments
     * @throws ImplerException
     */
    public static void main(String[] args) throws ImplerException {
        if (args == null || args.length < 2 || args.length > 3 || args[0] == null || args[1] == null
                || (args.length == 3 && (args[2] == null || !args[0].equals("-jar")))) {
            throw new ImplerException("Invalid arguments");
        }
        int i = args.length - 2;
        try {
            Class<?> token = Class.forName(args[i]);
            Path path = Paths.get(args[i + 1]);
            if (args.length == 3) {
                new JarImplementor().implementJar(token, path);
            } else {
                new JarImplementor().implement(token, path);
            }
        } catch (ClassNotFoundException e) {
            throw new ImplerException("Not found class: ", e);
        } catch (InvalidPathException e) {
            throw new ImplerException("Invalid path: ", e);
        }
    }

    /**
     * Create {@code .java} file with implementation of given class or interface {@code token}
     * in location specified by {@code root}.
     *
     * @param token type token to create implementation for
     * @param root location of {@code token}
     * @throws ImplerException if any error occur during implementation
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Arguments are null");
        }
        if (token.isPrimitive() || token.isArray() || token == Enum.class || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Error in token");
        }
        Path sourceCodeFile = FileUtils.createPath(root, token);
        FileUtils.createDirectory(sourceCodeFile);
        try (BufferedWriter writer = Files.newBufferedWriter(sourceCodeFile)) {
            writer.write(ImplementorUtils.generateSourceCode(token));
        } catch (IOException e) {
            throw new ImplerException("Error in writing: ", e);
        }
    }

    /**
     * Create {@code .jar} file with implementation of given class or interface {@code token}
     * in location specified by {@code jarFile}.
     * @param token type token to create implementation for
     * @param jarFile location of {@code .jar} file
     * @throws ImplerException if any error occur during implementation
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Arguments are null");
        }
        Path path = jarFile.toAbsolutePath().normalize();
        FileUtils.createDirectory(path);
        Path tmpPath = FileUtils.createTmpDirectory(path);
        try {
            implement(token, tmpPath);
            JarImplementorUtils.compile(token, tmpPath);
            JarImplementorUtils.createJar(token, tmpPath, jarFile);
        } finally {
            FileUtils.deleteTmpDirectory(tmpPath);
        }
    }
}
