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

class FakeNtfyConnectionTest {

    private FakeNtfyConnection fakeConnection;

    @BeforeEach
    void setUp() {
        fakeConnection = new FakeNtfyConnection();
    }

    @Test
    @DisplayName("send should store message and return success")
    void send_ShouldStoreMessageAndReturnSuccess() {
        // Act
        boolean result = fakeConnection.send("Test message");

        // Assert
        assertThat(result).isTrue();
        assertThat(fakeConnection.getSentMessages()).containsExactly("Test message");
    }

    @Test
    @DisplayName("send should return false when shouldSucceed is false")
    void send_ShouldReturnFalse_WhenShouldSucceedIsFalse() {
        // Arrange
        fakeConnection.setShouldSucceed(false);

        // Act
        boolean result = fakeConnection.send("Test message");

        // Assert
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentMessages()).containsExactly("Test message");
    }

    @Test
    @DisplayName("sendFile should store file and return success for existing file")
    void sendFile_ShouldStoreFileAndReturnSuccess(@TempDir java.nio.file.Path tempDir) throws IOException {
        // Arrange
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        // Act
        boolean result = fakeConnection.sendFile(testFile);

        // Assert
        assertThat(result).isTrue();
        assertThat(fakeConnection.getSentFiles()).containsExactly(testFile);
    }

    @Test
    @DisplayName("sendFile should return false for non-existent file")
    void sendFile_ShouldReturnFalseForNonExistentFile() {
        // Act
        boolean result = fakeConnection.sendFile(new File("non_existent.txt"));

        // Assert
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).isEmpty();
    }

    @Test
    @DisplayName("sendFile should return false for null file")
    void sendFile_ShouldReturnFalseForNullFile() {
        // Act
        boolean result = fakeConnection.sendFile(null);

        // Assert
        assertThat(result).isFalse();
        assertThat(fakeConnection.getSentFiles()).isEmpty();
    }

    @Test
    @DisplayName("receive should set message handler")
    void receive_ShouldSetMessageHandler() {
        // Arrange
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        // Act
        fakeConnection.receive(message -> handlerCalled.set(true));

        // Assert - simulate a message to verify handler is set
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null));
        assertThat(handlerCalled.get()).isTrue();
    }

    @Test
    @DisplayName("simulateIncomingMessage should call registered handler")
    void simulateIncomingMessage_ShouldCallRegisteredHandler() {
        // Arrange
        AtomicReference<NtfyMessageDto> receivedMessage = new AtomicReference<>();
        fakeConnection.receive(receivedMessage::set);
        NtfyMessageDto testMessage = new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null);

        // Act
        fakeConnection.simulateIncomingMessage(testMessage);

        // Assert
        assertThat(receivedMessage.get()).isEqualTo(testMessage);
    }

    @Test
    @DisplayName("simulateIncomingMessage should not throw when no handler is set")
    void simulateIncomingMessage_ShouldNotThrow_WhenNoHandlerIsSet() {
        // Arrange
        NtfyMessageDto testMessage = new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null);

        // Act & Assert - Should not throw exception
        fakeConnection.simulateIncomingMessage(testMessage);
    }

    @Test
    @DisplayName("clear should reset all state")
    void clear_ShouldResetAllState() {
        // Arrange
        fakeConnection.receive(msg -> {});
        fakeConnection.send("Test message");

        // Act
        fakeConnection.clear();

        // Assert
        assertThat(fakeConnection.getSentMessages()).isEmpty();
        assertThat(fakeConnection.getSentFiles()).isEmpty();
        // Verify simulateIncomingMessage doesn't crash after clear
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("1", 123L, "message", "topic", "test", null, null));
    }

    @Test
    @DisplayName("getSentMessages should return copy of sent messages")
    void getSentMessages_ShouldReturnCopy() {
        // Arrange
        fakeConnection.send("Message 1");

        // Act
        List<String> messages1 = fakeConnection.getSentMessages();
        fakeConnection.send("Message 2");
        List<String> messages2 = fakeConnection.getSentMessages();

        // Assert
        assertThat(messages1).containsExactly("Message 1");
        assertThat(messages2).containsExactly("Message 1", "Message 2");
    }

    @Test
    @DisplayName("getSentFiles should return copy of sent files")
    void getSentFiles_ShouldReturnCopy(@TempDir java.nio.file.Path tempDir) throws IOException {
        // Arrange
        File file1 = tempDir.resolve("file1.txt").toFile();
        Files.writeString(file1.toPath(), "Content 1");
        fakeConnection.sendFile(file1);

        // Act
        List<File> files1 = fakeConnection.getSentFiles();
        File file2 = tempDir.resolve("file2.txt").toFile();
        Files.writeString(file2.toPath(), "Content 2");
        fakeConnection.sendFile(file2);
        List<File> files2 = fakeConnection.getSentFiles();

        // Assert
        assertThat(files1).containsExactly(file1);
        assertThat(files2).containsExactly(file1, file2);
    }

    @Test
    @DisplayName("shouldSucceed should be true by default")
    void shouldSucceed_ShouldBeTrueByDefault() {
        // Act
        boolean result = fakeConnection.send("Test");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("setShouldSucceed should affect both send and sendFile")
    void setShouldSucceed_ShouldAffectBothSendAndSendFile(@TempDir java.nio.file.Path tempDir) throws IOException {
        // Arrange
        fakeConnection.setShouldSucceed(false);
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        // Act
        boolean sendResult = fakeConnection.send("Test message");
        boolean sendFileResult = fakeConnection.sendFile(testFile);

        // Assert
        assertThat(sendResult).isFalse();
        assertThat(sendFileResult).isFalse();
        assertThat(fakeConnection.getSentMessages()).containsExactly("Test message");
        assertThat(fakeConnection.getSentFiles()).containsExactly(testFile);
    }

    @Test
    @DisplayName("should handle multiple message handlers sequentially")
    void shouldHandleMultipleMessageHandlers_Sequentially() {
        // Arrange
        AtomicInteger handler1Count = new AtomicInteger(0);
        AtomicInteger handler2Count = new AtomicInteger(0);

        // Act - Set first handler, send message, then set second handler
        fakeConnection.receive(msg -> handler1Count.incrementAndGet());
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("1", 123L, "message", "topic", "test1", null, null));

        fakeConnection.receive(msg -> handler2Count.incrementAndGet());
        fakeConnection.simulateIncomingMessage(new NtfyMessageDto("2", 124L, "message", "topic", "test2", null, null));

        // Assert
        assertThat(handler1Count.get()).isEqualTo(1); // Should only receive first message
        assertThat(handler2Count.get()).isEqualTo(1); // Should only receive second message
    }

    @Test
    @DisplayName("should simulate real open event")
    void shouldSimulateRealOpenEvent() {
        // Arrange
        AtomicReference<NtfyMessageDto> receivedMessage = new AtomicReference<>();
        fakeConnection.receive(receivedMessage::set);

        NtfyMessageDto openEvent = new NtfyMessageDto(
                "H17EHDF9nohk",
                1763200648L,
                "open",
                "mytopic",
                null,
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(openEvent);

        // Assert
        assertThat(receivedMessage.get()).isEqualTo(openEvent);
        assertThat(receivedMessage.get().event()).isEqualTo("open");
    }

    @Test
    @DisplayName("should simulate real keepalive event")
    void shouldSimulateRealKeepaliveEvent() {
        // Arrange
        AtomicReference<NtfyMessageDto> receivedMessage = new AtomicReference<>();
        fakeConnection.receive(receivedMessage::set);

        NtfyMessageDto keepaliveEvent = new NtfyMessageDto(
                "bzLISfMxUlj3",
                1763200859L,
                "keepalive",
                "mytopic",
                null,
                null,
                null
        );

        // Act
        fakeConnection.simulateIncomingMessage(keepaliveEvent);

        // Assert
        assertThat(receivedMessage.get()).isEqualTo(keepaliveEvent);
        assertThat(receivedMessage.get().event()).isEqualTo("keepalive");
    }
}