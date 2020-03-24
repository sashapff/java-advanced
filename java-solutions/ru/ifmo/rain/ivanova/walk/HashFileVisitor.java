package ru.ifmo.rain.ivanova.walk;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class HashFileVisitor extends SimpleFileVisitor<Path> {
    private BufferedWriter writer;
    byte[] buffer = new byte[1024];

    HashFileVisitor(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        print(hash(file.toString()), file.toString());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        print(0, file.toString());
        return FileVisitResult.CONTINUE;
    }

    private int hash(String path) {
        try (FileInputStream fileReader = new FileInputStream(path)) {
            int hval = 0x811c9dc5;
            int FNV_32_PRIME = 0x01000193;
            try {
                int read;
                while ((read = fileReader.read(buffer)) > 0) {
                    for (int i = 0; i < read; i++) {
                        hval *= FNV_32_PRIME;
                        hval ^= (buffer[i] & 0xff);
                    }
                }
                return hval;
            } catch (IOException e) {
                System.err.println("Error in reading: " + e.getMessage());
                return 0;
            }
        } catch (IOException e) {
            System.err.println("Not found file: " + e.getMessage());
            return 0;
        }
    }

    void print(int hash, String s) throws IOException {
        writer.write(String.format("%08x %s%n", hash, s));
    }
}