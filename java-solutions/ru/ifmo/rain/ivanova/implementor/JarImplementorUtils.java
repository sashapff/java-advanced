package ru.ifmo.rain.ivanova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Class providing work with compiling and creating {@code .jar} for {@link JarImplementor}.
 *
 * @author sasha.pff
 * @version 1.0
 */
class JarImplementorUtils {
    /**
     * Suffix in name of implementing {@code .jar} files.
     */
    private static final String IMPL_CLASS_SUFFIX = "Impl.class";

    /**
     * Method to compile implemented class extending or implementing {@code token} and save in {@tmpPath} temporary
     * directory.
     *
     * @param token type token to compile
     * @param tmpPath temporary directory to save new {@code .class} file
     * @throws ImplerException if any error occur during compilation
     */
    static void compile(Class<?> token, Path tmpPath) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Error in getting compiler");
        }
        Path path;
        try {
            CodeSource codeSource = token.getProtectionDomain().getCodeSource();
            path = Paths.get(codeSource == null ? "" : codeSource.getLocation().getPath());
        } catch (InvalidPathException e) {
            throw new ImplerException("Error in getting class path: ", e);
        }
        String[] arguments = new String[]{
                "-cp",
                tmpPath.toString() + File.pathSeparator + path.toString(),
                FileUtils.createPath(tmpPath, token).toString()
        };
        if (compiler.run(null, null, null, arguments) != 0) {
            throw new ImplerException("Error in compiling");
        }
    }

    /**
     * Method to create {@code .jar} of compiled by {@link #compile(Class, Path)} class extending or
     * implementing {@code token} using {@link Manifest}.
     *
     * @param token type token to create {@code .jar}
     * @param tmpPath temporary directory with {@code .class} file
     * @param jarFile path to save {@code .jar} file
     * @throws ImplerException if any error occur during creating {@code .jar}
     */
    static void createJar(Class<?> token, Path tmpPath, Path jarFile) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            String path = String.join(File.separator, token.getPackageName().split("\\.")) +
                    File.separator +
                    token.getSimpleName() +
                    IMPL_CLASS_SUFFIX;
            stream.putNextEntry(new ZipEntry(path));
            Files.copy(Paths.get(tmpPath.toString(), path), stream);
        } catch (IOException e) {
            throw new ImplerException("Error in creating jar: ", e);
        }
    }
}
