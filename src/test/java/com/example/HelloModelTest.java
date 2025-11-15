package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HelloModelTest {

    private FakeNtfyConnection fakeConnection;
    private HelloModel model;
    private TestErrorHandler testErrorHandler;

    // Test error handler som samlar felmeddelanden
    static class TestErrorHandler implements HelloModel.ErrorHandler {
        private final List<String> errors = new ArrayList<>();

        @Override
        public void showError(String message) {
            errors.add(message);
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public void clear() {
            errors.clear();
        }
    }

    @BeforeEach
    void setUp() {
        fakeConnection = new FakeNtfyConnection();
        testErrorHandler = new TestErrorHandler();
        // Använd test-konstruktorn som tar både connection och error handler
        model = new HelloModel(fakeConnection, testErrorHandler);
    }

    @Test
    @DisplayName("sendMessage should call connection.send with correct message")
    void sendMessage_ShouldCallConnectionSend() {
        // Act
        model.sendMessage("Hello World");

        // Assert
        assertThat(fakeConnection.getSentMessages()).containsExactly("Hello World");
        assertThat(testErrorHandler.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("sendMessage should show error when connection fails")
    void sendMessage_ShouldShowError_WhenConnectionFails() {
        // Arrange
        fakeConnection.setShouldSucceed(false);

        // Act
        model.sendMessage("Test message");

        // Assert
        assertThat(fakeConnection.getSentMessages()).containsExactly("Test message");
        assertThat(testErrorHandler.getErrors()).containsExactly("Kunde inte skicka meddelandet");
    }

    @Test
    @DisplayName("sendFile should call connection.sendFile with correct file")
    void sendFile_ShouldCallConnectionSendFile(@TempDir Path tempDir) throws IOException {
        // Arrange
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        // Act
        boolean result = model.sendFile(testFile);

        // Assert
        assertThat(result).isTrue();
        assertThat(fakeConnection.getSentFiles()).containsExactly(testFile);
        assertThat(testErrorHandler.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("sendFile should return false for non-existent file")
    void sendFile_ShouldReturnFalseForNonExistentFile() {
        // Arrange
        File nonExistentFile = new File("non_existent_file.txt");

        // Act
        boolean result = model.sendFile(nonExistentFile);

        // Assert
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).isEmpty();
        assertThat(testErrorHandler.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("incoming messages should be added to observable list")
    void incomingMessages_ShouldBeAddedToObservableList() {
        // Arrange
        NtfyMessageDto testMessage = createTestMessage("Test incoming message");

        // Act
        fakeConnection.simulateIncomingMessage(testMessage);

        // Assert
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0)).isEqualTo(testMessage);
    }

    @Test
    @DisplayName("messages with attachments should be handled correctly")
    void messagesWithAttachments_ShouldBeHandledCorrectly() {
        // Arrange
        Attachment attachment = new Attachment("test.txt", "http://example.com/file.txt", "text/plain", 100L);
        NtfyMessageDto messageWithAttachment = new NtfyMessageDto(
                "test-id",
                System.currentTimeMillis() / 1000,
                "message",
                "MY_TOPIC",
                "File message",
                null,
                attachment
        );

        // Act
        fakeConnection.simulateIncomingMessage(messageWithAttachment);

        // Assert
        assertThat(model.getMessages()).hasSize(1);
        NtfyMessageDto receivedMessage = model.getMessages().get(0);
        assertThat(receivedMessage.hasAttachment()).isTrue();
        assertThat(receivedMessage.getAttachmentName()).isEqualTo("test.txt");
    }

    @Test
    @DisplayName("system events should be filtered out")
    void systemEvents_ShouldBeFilteredOut() {
        // Arrange
        NtfyMessageDto systemMessage = new NtfyMessageDto(
                "123",
                System.currentTimeMillis() / 1000,
                "keepalive",  // system event, not "message"
                "MY_TOPIC",
                null,
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(systemMessage);

        // Assert - system messages should not be added
        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("multiple messages should be stored in order")
    void multipleMessages_ShouldBeStoredInOrder() {
        // Arrange
        NtfyMessageDto message1 = createTestMessage("First message");
        NtfyMessageDto message2 = createTestMessage("Second message");
        NtfyMessageDto message3 = createTestMessage("Third message");

        // Act
        fakeConnection.simulateIncomingMessage(message1);
        fakeConnection.simulateIncomingMessage(message2);
        fakeConnection.simulateIncomingMessage(message3);

        // Assert
        assertThat(model.getMessages()).hasSize(3);
        assertThat(model.getMessages().get(0)).isEqualTo(message1);
        assertThat(model.getMessages().get(1)).isEqualTo(message2);
        assertThat(model.getMessages().get(2)).isEqualTo(message3);
    }

    // Helper methods
    private NtfyMessageDto createTestMessage(String message) {
        return new NtfyMessageDto(
                "test-id",
                System.currentTimeMillis() / 1000,
                "message",
                "MY_TOPIC",
                message,
                null,
                null
        );
    }
}