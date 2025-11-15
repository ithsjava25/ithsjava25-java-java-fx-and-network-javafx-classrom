package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FakeNtfyConnection}.
 * Verifies sending messages/files, receiving messages, and internal state behavior.
 */
class FakeNtfyConnectionTest {

    private FakeNtfyConnection fakeConnection;

    @BeforeEach
    void setUp() {
        fakeConnection = new FakeNtfyConnection();
    }

    @Test
    @DisplayName("send should store message and return success")
    void send_ShouldStoreMessageAndReturnSuccess() {
        boolean result = fakeConnection.send("Test message");
        assertThat(result).isTrue();
        assertThat(fakeConnection.getSentMessages()).containsExactly("Test message");
    }

    @Test
    @DisplayName("send should return false when shouldSucceed is false")
    void send_ShouldReturnFalse_WhenShouldSucceedIsFalse() {
        fakeConnection.setShouldSucceed(false);
        boolean result = fakeConnection.send("Test message");
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentMessages()).containsExactly("Test message");
    }

    @Test
    @DisplayName("sendFile should store file and return success for existing file")
    void sendFile_ShouldStoreFileAndReturnSuccess(@TempDir java.nio.file.Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        boolean result = fakeConnection.sendFile(testFile);

        assertThat(result).isTrue();
        assertThat(fakeConnection.getSentFiles()).containsExactly(testFile);
    }

    @Test
    @DisplayName("sendFile should return false for non-existent file")
    void sendFile_ShouldReturnFalseForNonExistentFile() {
        boolean result = fakeConnection.sendFile(new File("non_existent.txt"));
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).isEmpty();
    }

    @Test
    @DisplayName("sendFile should return false for null file")
    void sendFile_ShouldReturnFalseForNullFile() {
        boolean result = fakeConnection.sendFile(null);
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).isEmpty();
    }

    @Test
    @DisplayName("receive should set message handler")
    void receive_ShouldSetMessageHandler() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        fakeConnection.receive(message -> handlerCalled.set(true));
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null));
        assertThat(handlerCalled.get()).isTrue();
    }

    @Test
    @DisplayName("simulateIncomingMessage should call registered handler")
    void simulateIncomingMessage_ShouldCallRegisteredHandler() {
        AtomicReference<NtfyMessageDto> receivedMessage = new AtomicReference<>();
        fakeConnection.receive(receivedMessage::set);

        NtfyMessageDto testMessage = new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null);
        fakeConnection.simulateIncomingMessage(testMessage);

        assertThat(receivedMessage.get()).isEqualTo(testMessage);
    }

    @Test
    @DisplayName("simulateIncomingMessage should not throw when no handler is set")
    void simulateIncomingMessage_ShouldNotThrow_WhenNoHandlerIsSet() {
        NtfyMessageDto testMessage = new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null);
        fakeConnection.simulateIncomingMessage(testMessage); // No exception should occur
    }

    @Test
    @DisplayName("clear should reset all state")
    void clear_ShouldResetAllState() {
        fakeConnection.send("Test message");
        fakeConnection.receive(msg -> {});

        fakeConnection.clear();

        assertThat(fakeConnection.getSentMessages()).isEmpty();
        assertThat(fakeConnection.getSentFiles()).isEmpty();

        // simulateIncomingMessage should not throw
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null));
    }

    @Test
    @DisplayName("getSentMessages should return copy of sent messages")
    void getSentMessages_ShouldReturnCopy() {
        fakeConnection.send("Message 1");

        List<String> messages1 = fakeConnection.getSentMessages();
        fakeConnection.send("Message 2");
        List<String> messages2 = fakeConnection.getSentMessages();

        assertThat(messages1).containsExactly("Message 1");
        assertThat(messages2).containsExactly("Message 1", "Message 2");
    }

    @Test
    @DisplayName("getSentFiles should return copy of sent files")
    void getSentFiles_ShouldReturnCopy(@TempDir java.nio.file.Path tempDir) throws IOException {
        File file1 = tempDir.resolve("file1.txt").toFile();
        Files.writeString(file1.toPath(), "Content 1");
        fakeConnection.sendFile(file1);

        List<File> files1 = fakeConnection.getSentFiles();

        File file2 = tempDir.resolve("file2.txt").toFile();
        Files.writeString(file2.toPath(), "Content 2");
        fakeConnection.sendFile(file2);

        List<File> files2 = fakeConnection.getSentFiles();

        assertThat(files1).containsExactly(file1);
        assertThat(files2).containsExactly(file1, file2);
    }

    @Test
    @DisplayName("shouldSucceed should be true by default")
    void shouldSucceed_ShouldBeTrueByDefault() {
        assertThat(fakeConnection.send("Test")).isTrue();
    }

    @Test
    @DisplayName("should handle multiple message handlers sequentially")
    void shouldHandleMultipleMessageHandlers_Sequentially() {
        AtomicInteger handler1Count = new AtomicInteger(0);
        AtomicInteger handler2Count = new AtomicInteger(0);

        // First handler
        fakeConnection.receive(msg -> handler1Count.incrementAndGet());
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("1", 123L, "message", "topic", "test1", null, null));

        // Second handler overwrites first
        fakeConnection.receive(msg -> handler2Count.incrementAndGet());
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("2", 124L, "message", "topic", "test2", null, null));

        assertThat(handler1Count.get()).isEqualTo(1); // Received only the first message
        assertThat(handler2Count.get()).isEqualTo(1); // Received only the second message
    }
}
