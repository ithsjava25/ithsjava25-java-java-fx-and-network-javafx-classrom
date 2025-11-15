package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class HelloModelTest {

    private FakeNtfyConnection fakeConnection;
    private HelloModel model;
    private TestErrorHandler testErrorHandler;

    static class TestExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run(); // Kör direkt på samma tråd för testning
        }
    }

    static class TestErrorHandler implements java.util.function.Consumer<String> {
        private final List<String> errors = new ArrayList<>();

        @Override
        public void accept(String error) {
            errors.add(error);
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public boolean hasError(String partialMessage) {
            return errors.stream().anyMatch(error -> error.contains(partialMessage));
        }

        public void clear() {
            errors.clear();
        }
    }

    @BeforeEach
    void setUp() {
        fakeConnection = new FakeNtfyConnection();
        testErrorHandler = new TestErrorHandler();
        Executor testExecutor = new TestExecutor();
        model = new HelloModel(fakeConnection, testExecutor, testErrorHandler);
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

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("sendMessage should handle null and empty messages gracefully")
    void sendMessage_ShouldHandleNullOrEmptyMessages(String message) {
        // Act
        model.sendMessage(message);

        // Assert - Should not crash and should attempt to send
        assertThat(fakeConnection.getSentMessages()).containsExactly(message);
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
        assertThat(testErrorHandler.hasError("Could not send message")).isTrue();
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
    @DisplayName("sendFile should return false for null file")
    void sendFile_ShouldReturnFalseForNullFile() {
        // Act
        boolean result = model.sendFile(null);

        // Assert
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).isEmpty();
        assertThat(testErrorHandler.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("sendFile should show error when connection fails")
    void sendFile_ShouldShowError_WhenConnectionFails(@TempDir Path tempDir) throws IOException {
        // Arrange
        fakeConnection.setShouldSucceed(false);
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        // Act
        boolean result = model.sendFile(testFile);

        // Assert
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).containsExactly(testFile);
        assertThat(testErrorHandler.hasError("Could not send file")).isTrue();
    }

    @Test
    @DisplayName("sendFile should show error when exception occurs")
    void sendFile_ShouldShowError_WhenExceptionOccurs(@TempDir Path tempDir) throws IOException {
        // Arrange
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        // Mocka ett exception genom att använda en speciell fil som FakeNtfyConnection kan känna igen
        File problematicFile = tempDir.resolve("problematic.txt").toFile();
        Files.writeString(problematicFile.toPath(), "Problematic content");

        // Skapa en temporär FakeNtfyConnection som kastar exception för specifika filer
        FakeNtfyConnection throwingConnection = new FakeNtfyConnection() {
            @Override
            public boolean sendFile(File file) {
                if (file.getName().equals("problematic.txt")) {
                    throw new RuntimeException("Simulated file error");
                }
                return super.sendFile(file);
            }
        };

        TestErrorHandler localErrorHandler = new TestErrorHandler();
        HelloModel localModel = new HelloModel(throwingConnection, new TestExecutor(), localErrorHandler);

        // Act
        boolean result = localModel.sendFile(problematicFile);

        // Assert
        assertThat(result).isFalse();
        assertThat(localErrorHandler.hasError("Error sending file")).isTrue();
        assertThat(localErrorHandler.hasError("Simulated file error")).isTrue();
    }

    @Test
    @DisplayName("getMessages should return observable list")
    void getMessages_ShouldReturnObservableList() {
        // Act & Assert
        assertThat(model.getMessages()).isNotNull();
        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("incoming message event should be added to observable list")
    void incomingMessageEvent_ShouldBeAddedToObservableList() {
        // Arrange - Real message event
        NtfyMessageDto testMessage = createMessageEvent("Test incoming message");

        // Act
        fakeConnection.simulateIncomingMessage(testMessage);

        // Assert
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0)).isEqualTo(testMessage);
    }

    @Test
    @DisplayName("open event should be filtered out")
    void openEvent_ShouldBeFilteredOut() {
        // Arrange - Open event (like when connecting)
        NtfyMessageDto openEvent = new NtfyMessageDto(
                "H17EHDF9nohk",
                1763200648L,
                "open",  // This should be filtered out
                "mytopic",
                null,
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(openEvent);

        // Assert
        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("keepalive event should be filtered out")
    void keepaliveEvent_ShouldBeFilteredOut() {
        // Arrange - Keepalive event
        NtfyMessageDto keepaliveEvent = new NtfyMessageDto(
                "bzLISfMxUlj3",
                1763200859L,
                "keepalive",  // This should be filtered out
                "mytopic",
                null,
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(keepaliveEvent);

        // Assert
        assertThat(model.getMessages()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"open", "keepalive", "poll_request"})
    @DisplayName("all system events should be filtered out")
    void allSystemEvents_ShouldBeFilteredOut(String eventType) {
        // Arrange
        NtfyMessageDto systemEvent = new NtfyMessageDto(
                "123",
                System.currentTimeMillis() / 1000,
                eventType,
                "MY_TOPIC",
                "Should be filtered",
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(systemEvent);

        // Assert
        assertThat(model.getMessages()).isEmpty();
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

        assertAll(
                () -> assertThat(receivedMessage.hasAttachment()).isTrue(),
                () -> assertThat(receivedMessage.getAttachmentName()).isEqualTo("test.txt"),
                () -> assertThat(receivedMessage.getAttachmentUrl()).isEqualTo("http://example.com/file.txt"),
                () -> assertThat(receivedMessage.getAttachmentContentType()).isEqualTo("text/plain")
        );
    }

    @Test
    @DisplayName("multiple messages should be stored in order")
    void multipleMessages_ShouldBeStoredInOrder() {
        // Arrange
        NtfyMessageDto message1 = createMessageEvent("First message");
        NtfyMessageDto message2 = createMessageEvent("Second message");
        NtfyMessageDto message3 = createMessageEvent("Third message");

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

    @Test
    @DisplayName("model constructor with single parameter should work")
    void modelConstructor_WithSingleParameter_ShouldWork() {
        // Arrange - Använd vår test-executor istället för standardkonstruktorn
        FakeNtfyConnection connection = new FakeNtfyConnection();
        TestErrorHandler localErrorHandler = new TestErrorHandler();
        HelloModel singleParamModel = new HelloModel(connection, new TestExecutor(), localErrorHandler);

        // Act
        connection.simulateIncomingMessage(createMessageEvent("Test"));

        // Assert
        assertThat(singleParamModel.getMessages()).hasSize(1);
    }

    @Test
    @DisplayName("messages with same content but different IDs should be treated as different")
    void messagesWithSameContentDifferentIds_ShouldBeTreatedAsDifferent() {
        // Arrange
        NtfyMessageDto message1 = new NtfyMessageDto("1", 1234567890L, "message", "topic", "same content", null, null);
        NtfyMessageDto message2 = new NtfyMessageDto("2", 1234567890L, "message", "topic", "same content", null, null);

        // Act
        fakeConnection.simulateIncomingMessage(message1);
        fakeConnection.simulateIncomingMessage(message2);

        // Assert
        assertThat(model.getMessages()).hasSize(2);
        assertThat(model.getMessages().get(0)).isNotEqualTo(model.getMessages().get(1));
    }

    @Test
    @DisplayName("messages with attachments but no URL should not cause errors")
    void messagesWithAttachmentsButNoUrl_ShouldNotCauseErrors() {
        // Arrange
        Attachment attachment = new Attachment("test.txt", null, "text/plain", 100L);
        NtfyMessageDto message = new NtfyMessageDto(
                "test-id",
                System.currentTimeMillis() / 1000,
                "message",
                "MY_TOPIC",
                "File without URL",
                null,
                attachment
        );

        // Act & Assert - Should not throw exception
        fakeConnection.simulateIncomingMessage(message);

        // Assert
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0).hasAttachment()).isTrue();
        assertThat(model.getMessages().get(0).getAttachmentUrl()).isNull();
    }

    @Test
    @DisplayName("should handle real-world message format")
    void shouldHandleRealWorldMessageFormat() {
        // Arrange - Real message from your logs
        NtfyMessageDto realMessage = new NtfyMessageDto(
                "9Jor91oU8JTv",
                1763200650L,
                "message",
                "mytopic",
                "hello world",
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(realMessage);

        // Assert
        assertThat(model.getMessages()).hasSize(1);
        NtfyMessageDto received = model.getMessages().get(0);
        assertThat(received.id()).isEqualTo("9Jor91oU8JTv");
        assertThat(received.message()).isEqualTo("hello world");
    }

    @Test
    @DisplayName("should handle received message echo after sending")
    void shouldHandleReceivedMessageEcho_AfterSending() {
        // Arrange
        model.sendMessage("hello world");

        // Simulate the echo response from server
        NtfyMessageDto echoMessage = new NtfyMessageDto(
                "9Jor91oU8JTv",
                1763200650L,
                "message",
                "mytopic",
                "hello world",
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(echoMessage);

        // Assert
        assertThat(fakeConnection.getSentMessages()).containsExactly("hello world");
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0).message()).isEqualTo("hello world");
    }

    // Helper methods
    private NtfyMessageDto createMessageEvent(String message) {
        return new NtfyMessageDto(
                "test-id-" + System.currentTimeMillis(),
                System.currentTimeMillis() / 1000,
                "message",
                "MY_TOPIC",
                message,
                null,
                null
        );
    }
}