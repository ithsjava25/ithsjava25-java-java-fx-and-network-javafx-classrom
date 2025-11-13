package com.example;

public class HelloModel {

    /**
     * Builds a greeting string that includes the JavaFX and Java runtime versions.
     *
     * <p>The method reads the system properties "javafx.version" and "java.version" and inserts their
     * values into the greeting text.
     *
     * @return the greeting in the form "Hello, JavaFX {javafxVersion}, running on Java {javaVersion}."
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }
}