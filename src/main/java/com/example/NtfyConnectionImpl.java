package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean sendMessage(String message) {
        //Todo: Send message using HTTPClient

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .build();
        try {
            //Todo: handle long blocking send requests to not freeze the JavaFX thread
            //1. Use thread send message?
            //2. Use async?
            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("Failed to send message. HTTP Status: " + response.statusCode());
                System.err.println("Response body: " + response.body());
                return false;
            }
            return true;
        } catch (IOException e) {
            System.err.println("Network I/O error sending message: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Send message operation interrupted");
            Thread.currentThread().interrupt();
        }
        return false;
    }


    @Override
    public void receiveMessage(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        System.err.println("Error connecting to stream. HTTP Status: " + response.statusCode());
                        return;
                    }
                    response.body()
                            .map(s -> {
                                try {
                                    return mapper.readValue(s, NtfyMessageDto.class);
                                } catch (Exception e) {
                                    System.err.println("Failed to parse JSON: " + e.getMessage());
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .filter(message -> "message".equals(message.event()))
                            .peek(m -> {
                                var time = java.time.Instant.ofEpochSecond(m.time())
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalTime();
                                System.out.println(time + " " + m.message());
                            })
                            .forEach(messageHandler);
                }).exceptionally(e -> {
                    System.err.println("Async connection error: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                });
    }
}

