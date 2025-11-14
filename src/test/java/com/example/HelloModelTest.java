package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    private NtfyConnectionSpy spy;
    private HelloModel model;

    @BeforeEach
    void setUp() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {

        model.setMessageToSend("Hello World");


        boolean result = model.sendMessage();


        assertThat(result).isTrue();
        assertThat(spy.message).isEqualTo("Hello World");
        assertThat(spy.sendCallCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Empty message should not be sent")
    void emptyMessageShouldNotBeSent() {
        // Arrange
        model.setMessageToSend("");


        boolean result = model.sendMessage();


        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
        assertThat(spy.sendCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Null message should not be sent")
    void nullMessageShouldNotBeSent() {

        model.setMessageToSend(null);


        boolean result = model.sendMessage();


        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
        assertThat(spy.sendCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Whitespace only message should not be sent")
    void whitespaceMessageShouldNotBeSent() {
        // Arrange
        model.setMessageToSend("   ");


        boolean result = model.sendMessage();


        assertThat(result).isFalse();
        assertThat(spy.message).isNull();
        assertThat(spy.sendCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Send file calls connection with file")
    void sendFileCallsConnectionWithFile() throws IOException {

        File tempFile = File.createTempFile("test", ".txt");
        Files.write(tempFile.toPath(), "Test content".getBytes());


        boolean result = model.sendFile(tempFile);


        assertThat(result).isTrue();
        assertThat(spy.file).isNotNull();
        assertThat(spy.file).isEqualTo(tempFile); // Testa att samma fil skickas
        assertThat(spy.sendFileCallCount).isEqualTo(1);


        tempFile.delete();
    }

    @Test
    @DisplayName("Send null file should not call connection")
    void sendNullFileShouldNotCallConnection() {

        boolean result = model.sendFile(null);


        assertThat(result).isFalse();
        assertThat(spy.file).isNull();
        assertThat(spy.sendFileCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Send non-existent file should not call connection")
    void sendNonExistentFileShouldNotCallConnection() {

        File nonExistentFile = new File("non_existent_file.txt");


        boolean result = model.sendFile(nonExistentFile);


        assertThat(result).isFalse();
        assertThat(spy.file).isNull();
        assertThat(spy.sendFileCallCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Receive message adds message to list")
    void receiveMessageAddsMessageToList() {

        NtfyMessageDto testMessage = new NtfyMessageDto(
                "test-id",
                System.currentTimeMillis(),
                "message",
                "mytopic",
                "Test message from spy",
                null,
                null
        );


        model.addTestMessage(testMessage);


        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0).message()).isEqualTo("Test message from spy");
    }
}