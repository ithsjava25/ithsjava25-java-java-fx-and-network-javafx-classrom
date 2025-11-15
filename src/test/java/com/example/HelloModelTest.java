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
 * Tests message sending, file sending, message receiving, error handling, and event filtering.
 * Uses {@link FakeNtfyConnection} for isolated testing without real network calls.
 */
class HelloModelTest {

    private FakeNtfyConnection fakeConnection;
    private HelloModel model;
    private TestErrorHandler testErrorHandler;

    /**
     * Test implementation of {@link Executor} that executes commands immediately on the same thread.
     * Useful for testing without involving the JavaFX application thread.
     */
    static class TestExecutor implements Executor {
        /**
         * Executes the given command immediately on the same thread
         * @param command the runnable task to execute
         */
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    /**
     * Test implementation of error handler that collects error messages for verification.
     * Allows tests to check if specific errors occurred during test execution.
     */
    static class TestErrorHandler implements java.util.function.Consumer<String> {
        private final List<String> errors = new ArrayList<>();

        /**
         * Accepts and stores an error message
         * @param error the error message to store
         */
        @Override
        public void accept(String error) {
            errors.add(error);
        }

        /**
         * Gets all stored error messages
         * @return a copy of the list of error messages
         */
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        /**
         * Checks if any error contains the specified partial message
         * @param partialMessage the partial message to search for
         * @return true if any error contains the partial message, false otherwise
         */
        public boolean hasError(String partialMessage) {
            return errors.stream().anyMatch(error -> error.contains(partialMessage));
        }

        /**
         * Clears all stored error messages
         */
        public void clear() {
            errors.clear();
        }
    }

    /**
     * Sets up test fixtures before each test method.
     * Initializes a fresh {@link FakeNtfyConnection}, {@link TestErrorHandler}, and {@link HelloModel} instance.
     */
    @BeforeEach
    void setUp() {
        fakeConnection = new FakeNtfyConnection();
        testErrorHandler = new TestErrorHandler();
        Executor testExecutor = new TestExecutor();
        model = new HelloModel(fakeConnection, testExecutor, testErrorHandler);
    }

    /**
     * Tests that sending a message correctly calls the connection's send method with the expected message.
     */
    @Test
    @DisplayName("sendMessage should call connection.send with correct message")
    void sendMessage_ShouldCallConnectionSend() {
        // Act
        model.sendMessage("Hello World");

        // Assert
        assertThat(fakeConnection.getSentMessages()).containsExactly("Hello World");
        assertThat(testErrorHandler.getErrors()).isEmpty();
    }

    /**
     * Tests that error handling works correctly when the connection fails to send a message.
     */
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

    /**
     * Tests that sending a file correctly calls the connection's sendFile method with the expected file.
     * @param tempDir temporary directory provided by JUnit for test file creation
     * @throws IOException if test file creation fails
     */
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

    /**
     * Tests that error handling works correctly when the connection fails to send a file.
     * @param tempDir temporary directory provided by JUnit for test file creation
     * @throws IOException if test file creation fails
     */
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

    /**
     * Tests that exceptions during file sending are properly caught and handled with error messages.
     * @param tempDir temporary directory provided by JUnit for test file creation
     * @throws IOException if test file creation fails
     */
    @Test
    @DisplayName("sendFile should show error when exception occurs")
    void sendFile_ShouldShowError_WhenExceptionOccurs(@TempDir Path tempDir) throws IOException {
        // Arrange
        File testFile = tempDir.resolve("test.txt").toFile();
        Files.writeString(testFile.toPath(), "Test content");

        // Create a temporary FakeNtfyConnection that throws exception for specific files
        File problematicFile = tempDir.resolve("problematic.txt").toFile();
        Files.writeString(problematicFile.toPath(), "Problematic content");

        /**
         * Custom FakeNtfyConnection that throws RuntimeException for specific files.
         * Used to simulate file sending exceptions.
         */
        FakeNtfyConnection throwingConnection = new FakeNtfyConnection() {
            /**
             * Sends a file, but throws RuntimeException for problematic files
             * @param file the file to send
             * @return true if successful, false otherwise
             * @throws RuntimeException if file name is "problematic.txt"
             */
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

    /**
     * Tests that the getMessages method returns a valid observable list that is initially empty.
     */
    @Test
    @DisplayName("getMessages should return observable list")
    void getMessages_ShouldReturnObservableList() {
        // Act & Assert
        assertThat(model.getMessages()).isNotNull();
        assertThat(model.getMessages()).isEmpty();
    }

    /**
     * Tests that incoming message events are properly added to the observable messages list.
     */
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

    /**
     * Tests that "open" system events are properly filtered out and not added to the messages list.
     */
    @Test
    @DisplayName("open event should be filtered out")
    void openEvent_ShouldBeFilteredOut() {
        // Arrange - Open event (like when connecting)
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
        assertThat(model.getMessages()).isEmpty();
    }

    /**
     * Tests that "keepalive" system events are properly filtered out and not added to the messages list.
     */
    @Test
    @DisplayName("keepalive event should be filtered out")
    void keepaliveEvent_ShouldBeFilteredOut() {
        // Arrange - Keepalive event
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
        assertThat(model.getMessages()).isEmpty();
    }

    /**
     * Parameterized test that verifies all system event types are filtered out.
     * @param eventType the type of system event to test (provided by ValueSource)
     */
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

    /**
     * Tests that messages with attachments are properly handled and attachment information is accessible.
     */
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

    /**
     * Tests that multiple incoming messages are stored in the correct order.
     */
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

    /**
     * Tests that the model constructor with dependency injection works correctly.
     */
    @Test
    @DisplayName("model constructor with single parameter should work")
    void modelConstructor_WithSingleParameter_ShouldWork() {
        // Arrange
        FakeNtfyConnection connection = new FakeNtfyConnection();
        TestErrorHandler localErrorHandler = new TestErrorHandler();
        HelloModel singleParamModel = new HelloModel(connection, new TestExecutor(), localErrorHandler);

        // Act
        connection.simulateIncomingMessage(createMessageEvent("Test"));

        // Assert
        assertThat(singleParamModel.getMessages()).hasSize(1);
    }

    /**
     * Tests that messages with identical content but different IDs are treated as distinct messages.
     */
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

    /**
     * Tests that real-world message formats (from actual logs) are handled correctly.
     */
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

    /**
     * Tests the scenario where a sent message is echoed back and properly handled as a received message.
     */
    @Test
    @DisplayName("should handle received message echo after sending")
    void shouldHandleReceivedMessageEcho_AfterSending() {
        // Arrange
        model.sendMessage("hello world");

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

    /**
     * Creates a test message event with the specified message text
     * @param message the message text to include in the event
     * @return a new NtfyMessageDto with test data
     */
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