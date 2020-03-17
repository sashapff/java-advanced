package ru.ifmo.rain.ivanova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class providing work with directories for {@link JarImplementor}.
 *
 * @author sasha.pff
 * @version 1.0
 */
class FileUtils {
    /**
     * Suffix in name of implementing {@code .java} files.
     */
    private static final String IMPL_JAVA_SUFFIX = "Impl.java";

    /**
     * Method to create directory in {@code path}.
     *
     * @param path location of new directory
     * @throws ImplerException if any error occur during creating directory
     */
    static void createDirectory(Path path) throws ImplerException {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new ImplerException("Error in creating directory: ", e);
        }
    }

    /**
     * Method to create temporary directory in {@code path}.
     *
     * @param path location on new temporary directory
     * @return resulting path
     * @throws ImplerException if any error occur during creating directory
     */
    static Path createTmpDirectory(Path path) throws ImplerException {
        try {
            return Files.createTempDirectory(path.getParent(), "tmp");
        } catch (IOException e) {
            throw new ImplerException("Error in creating temporary directory: ", e);
        }
    }

    /**
     * Method to delete directory of {@code file}.
     *
     * @param file directory to delete
     */
    static void deleteDirectory(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                deleteDirectory(f);
            }
        }
    }

    /**
     * Method to delete temporary directory of {@code tmpPath}.
     *
     * @param tmpPath temporary directory to delete
     */
    static void deleteTmpDirectory(Path tmpPath) {
        deleteDirectory(tmpPath.toFile());
    }

    /**
     * Method to create {@code path} of {@code .java} file implemented {@code token}.
     *
     * @param root location of new directory
     * @param token type token to create new file path
     * @return path of new {@code .java} file
     */
    static Path createPath(Path root, Class<?> token) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + IMPL_JAVA_SUFFIX);
    }
}
