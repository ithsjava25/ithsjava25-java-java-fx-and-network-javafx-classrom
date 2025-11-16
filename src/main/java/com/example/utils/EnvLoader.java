package com.example.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EnvLoader {
    public static Properties loadEnv() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Could not load .env file", e);
        }

        return props;
    }
}