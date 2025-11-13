package com.example.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EnvLoader {

    /**
     * Loads key=value pairs from a .env file in the current working directory into Java system properties.
     *
     * Each non-empty line that does not start with '#' and contains a single '=' is parsed; both key and
     * value are trimmed before the key/value pair is stored with System.setProperty.
     * If a .env file is not present, existing system properties are left unchanged.
     */
    public static void load() {
        Path envPath = Paths.get(".env");

        if (!Files.exists(envPath)) {
            System.out.println("No .env file found, using system environment variables");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    System.setProperty(key, value);
                    System.out.println("Loaded env: " + key + "=" + value);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load .env file: " + e.getMessage());
        }
    }
}