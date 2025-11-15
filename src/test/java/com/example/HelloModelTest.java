package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
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

/**
 * Unit tests for {@link HelloModel} class.
 * Tests message sending, file sending, message receiving, error handling, and attachment handling.
 * Uses {@link FakeNtfyConnection} for isolated testing without real network calls.
 */
class HelloModelTest {

    private FakeNtfyConnection fakeConnection;
    private HelloModel model;
    private TestErrorHandler testErrorHandler;

    /** Executes commands immediately on the same thread for testing. */
    static class TestExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    /** Collects error messages for verification. */
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
        model = new HelloModel(fakeConnection, new TestExecutor(), testErrorHandler);
    }

    @Test
    @DisplayName("sendMessage should call connection.send with correct message")
    void sendMessage_ShouldCallConnectionSend() {
        model.sendMessage("Hello World");
        assertThat(fakeConnection.getSentMessages()).containsExactly("Hello World");
        assertThat(testErrorHandler.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("sendMessage should show error when connection fails")
    void sendMessage_ShouldShowError_WhenConnectionFails() {
        fakeConnection.setShouldSucceed(false);
        model.sendMessage("Test message");
        assertThat(fakeConnection.getSentMessages()).containsExactly("Test message");
        assertThat(testErrorHandler.hasError("Could not send message")).isTrue();
    }

    @Test
    @DisplayName("sendFile should call connection.sendFile with correct file")
    void sendFile_ShouldCallConnectionSendFile(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("test.txt").toFile();
        Files.writeString(file.toPath(), "Test content");

        boolean result = model.sendFile(file);

        assertThat(result).isTrue();
        assertThat(fakeConnection.getSentFiles()).containsExactly(file);
        assertThat(testErrorHandler.getErrors()).isEmpty();
    }

    @Test
    @DisplayName("sendFile should show error when connection fails")
    void sendFile_ShouldShowError_WhenConnectionFails(@TempDir Path tempDir) throws IOException {
        fakeConnection.setShouldSucceed(false);
        File file = tempDir.resolve("test.txt").toFile();
        Files.writeString(file.toPath(), "Test content");

        boolean result = model.sendFile(file);

        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).containsExactly(file);
        assertThat(testErrorHandler.hasError("Could not send file")).isTrue();
    }

    @Test
    @DisplayName("sendFile should show error when exception occurs")
    void sendFile_ShouldShowError_WhenExceptionOccurs(@TempDir Path tempDir) throws IOException {
        File problematicFile = tempDir.resolve("problematic.txt").toFile();
        Files.writeString(problematicFile.toPath(), "Problematic content");

        FakeNtfyConnection throwingConnection = new FakeNtfyConnection() {
            @Override
            public boolean sendFile(File file) {
                if (file.getName().equals("problematic.txt")) {
                    throw new RuntimeException("Simulated file error");
                }
                return super.sendFile(file);
            }
        };

        TestErrorHandler localHandler = new TestErrorHandler();
        HelloModel localModel = new HelloModel(throwingConnection, new TestExecutor(), localHandler);

        boolean result = localModel.sendFile(problematicFile);

        assertThat(result).isFalse();
        assertThat(localHandler.hasError("Error sending file")).isTrue();
        assertThat(localHandler.hasError("Simulated file error")).isTrue();
    }

    @Test
    @DisplayName("getMessages should return observable list")
    void getMessages_ShouldReturnObservableList() {
        assertThat(model.getMessages()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("incoming message event should be added to observable list")
    void incomingMessageEvent_ShouldBeAddedToObservableList() {
        NtfyMessageDto msg = createMessageEvent("Test incoming message");
        fakeConnection.simulateIncomingMessage(msg);
        assertThat(model.getMessages()).containsExactly(msg);
    }

    @ParameterizedTest
    @ValueSource(strings = {"open", "keepalive", "poll_request"})
    @DisplayName("system events should be filtered out")
    void systemEvents_ShouldBeFilteredOut(String eventType) {
        NtfyMessageDto systemEvent = new NtfyMessageDto(
                "id", System.currentTimeMillis() / 1000, eventType, "topic", "ignored", null, null
        );
        fakeConnection.simulateIncomingMessage(systemEvent);
        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("messages with attachments should be handled correctly")
    void messagesWithAttachments_ShouldBeHandledCorrectly() {
        Attachment attachment = new Attachment("test.txt", "https://example.com/file.txt", "text/plain", 100L);
        NtfyMessageDto msg = new NtfyMessageDto("id", System.currentTimeMillis() / 1000,
                "message", "topic", "Has attachment", null, attachment);

        fakeConnection.simulateIncomingMessage(msg);

        NtfyMessageDto received = model.getMessages().get(0);

        assertAll(
                () -> assertThat(received.hasAttachment()).isTrue(),
                () -> assertThat(received.getAttachmentName()).isEqualTo("test.txt"),
                () -> assertThat(received.getAttachmentUrl()).isEqualTo("https://example.com/file.txt"),
                () -> assertThat(received.getAttachmentContentType()).isEqualTo("text/plain")
        );
    }

    @Test
    @DisplayName("multiple messages are stored in order")
    void multipleMessages_ShouldBeStoredInOrder() {
        NtfyMessageDto m1 = createMessageEvent("first");
        NtfyMessageDto m2 = createMessageEvent("second");
        NtfyMessageDto m3 = createMessageEvent("third");

        fakeConnection.simulateIncomingMessage(m1);
        fakeConnection.simulateIncomingMessage(m2);
        fakeConnection.simulateIncomingMessage(m3);

        assertThat(model.getMessages()).containsExactly(m1, m2, m3);
    }

    @Test
    @DisplayName("messages with same content but different IDs are distinct")
    void messagesWithSameContentDifferentIds_ShouldBeTreatedAsDifferent() {
        NtfyMessageDto m1 = new NtfyMessageDto("1", 123L, "message", "topic", "content", null, null);
        NtfyMessageDto m2 = new NtfyMessageDto("2", 123L, "message", "topic", "content", null, null);

        fakeConnection.simulateIncomingMessage(m1);
        fakeConnection.simulateIncomingMessage(m2);

        assertThat(model.getMessages()).hasSize(2);
        assertThat(model.getMessages().get(0)).isNotEqualTo(model.getMessages().get(1));
    }

    /** Creates a dummy test message */
    private NtfyMessageDto createMessageEvent(String message) {
        return new NtfyMessageDto("test-" + System.currentTimeMillis(),
                System.currentTimeMillis() / 1000,
                "message",
                "topic",
                message,
                null,
                null);
    }
}
