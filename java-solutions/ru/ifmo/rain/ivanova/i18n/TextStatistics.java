package ru.ifmo.rain.ivanova.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class TextStatistics {

    static Locale getLocale(final String arg) {
        final String[] args = arg.split("_");
        switch (args.length) {
            case 0:
                return Locale.getDefault();
            case 1:
                return new Locale(args[0]);
            case 2:
                return new Locale(args[0], args[1]);
            default:
                return new Locale(args[0], args[1], args[2]);
        }
    }

    private static ResourceBundle getBundle(final Locale locale) {
        return ResourceBundle.getBundle("ru.ifmo.rain.ivanova.i18n.UsageResourceBundle", locale);
    }

    static Path getPath(final String file) {
        return Paths.get("java-solutions/ru/ifmo/rain/ivanova/i18n/" + file);
    }

    private void write(final Locale inputLocale, final Locale outputLocale,
                       final String inputFile, final String outputFile) {
        try (
                final BufferedReader reader = Files.newBufferedReader(getPath(inputFile), StandardCharsets.UTF_8);
                final BufferedWriter writer = Files.newBufferedWriter(getPath(outputFile), StandardCharsets.UTF_8)
        ) {
            new TextWriter(writer, getBundle(outputLocale), new TextHandler(reader, inputLocale).handle())
                    .write(inputFile);
        } catch (IOException e) {
            System.out.println("Cant create reader or writer");
        }
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Incorrect arguments");
            System.out.println("Enter <input locale> <output locale> <input file> <output file>");
            return;
        }
        new TextStatistics().write(getLocale(args[0]), getLocale(args[1]), args[2], args[3]);
    }
}
