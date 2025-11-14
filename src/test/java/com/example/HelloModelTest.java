package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

    private HelloModel model;
    private NtfyConnectionSpy connectionSpy;

    @BeforeEach
    void setUp() {
        connectionSpy = new NtfyConnectionSpy();
        model = new HelloModel(connectionSpy);
    }

    @Test
    void sendMessage_callsConnection() {
        model.sendMessage("Hej");
        assertThat(connectionSpy.getLastMessage()).isEqualTo("Hej!");
    }

    @Test
    void sendFile_callsConnectionWithData() throws IOException {
        File tempFile = File.createTempFile("testfile", ".txt");
        Files.writeString(tempFile.toPath(), "Hello World");

        model.sendFile(tempFile);

        assertThat(connectionSpy.getLastFileName()).isEqualTo(tempFile.getName());
        assertThat(connectionSpy.getLastFileData()).isEqualTo(Files.readAllBytes(tempFile.toPath()));
    }
}
