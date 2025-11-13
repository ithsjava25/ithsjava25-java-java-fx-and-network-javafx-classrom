package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http;
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public void send(String message, Consumer<Boolean> callback) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .header("Cache", "no")
                .timeout(Duration.ofSeconds(10))  //request timeout
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> {
                    boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
                    callback.accept(success);
                })
                .exceptionally(throwable -> {
                    System.err.println("Error sending message: " + throwable.getMessage());
                    callback.accept(false);
                    return null;
                });
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .timeout(Duration.ofSeconds(30))  //timeout för receive
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    try {
                        response.body()
                                .map(line -> {
                                    try {
                                        return mapper.readValue(line, NtfyMessageDto.class);
                                    } catch (Exception e) {
                                        System.err.println("Failed to parse message: " + line);
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .forEach(messageHandler);
                    } catch (Exception e) {
                        System.err.println("Stream processing error: " + e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error receiving messages: " + ex.getMessage());
                    return null;
                });
    }
}