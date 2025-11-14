package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of NtfyConnection using HTTP for sending and receiving messages.
 */
public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final String userId;
    private String currentTopic;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Loads configuration from environment variables. */
    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        this.hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
        this.userId = Objects.requireNonNull(dotenv.get("USER_ID"), "USER_ID");
        this.currentTopic = dotenv.get("DEFAULT_TOPIC", "mytopic");
    }

    /** Creates connection with a custom host, default user and topic. */
    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
        this.userId = "testuser";
        this.currentTopic = "mytopic";
    }

    /** Creates connection with custom host, user, and topic. */
    public NtfyConnectionImpl(String hostName, String userId, String topic) {
        this.hostName = hostName;
        this.userId = userId;
        this.currentTopic = topic;
    }

    /** Returns the user ID. */
    public String getUserId() {
        return userId;
    }

    /** Returns the current topic. */
    public String getCurrentTopic() {
        return currentTopic;
    }

    /** Sets the current topic. */
    public void setCurrentTopic(String topic) {
        this.currentTopic = topic;
    }

    /**
     * Sends a message asynchronously to the current topic.
     * @param message message to send
     * @param callback called with true if successful, false otherwise
     */
    @Override
    public void send(String message, Consumer<Boolean> callback) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache", "no")
                .header("X-User-Id", userId)
                .uri(URI.create(hostName + "/" + currentTopic))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> response.statusCode() / 100 == 2)
                .exceptionally(ex -> {
                    System.out.println("Error sending message: " + ex.getMessage());
                    return false;
                })
                .thenAccept(callback);
    }

    /**
     * Receives messages from the current topic asynchronously.
     * @param messageHandler called for each received message
     */
    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/" + currentTopic + "/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (Exception e) {
                                System.out.println("Failed to parse message: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .peek(System.out::println)
                        .forEach(messageHandler));
    }
}