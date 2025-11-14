package com.example;

import io.github.cdimascio.dotenv.Dotenv;
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
        String loadedHostName = null;
        try {
            Dotenv dotenv = Dotenv.load();
            loadedHostName = dotenv.get("HOST_NAME");
        } catch (Exception e) {
            System.err.println("WARNING: Could not load .env file for HOST_NAME. Using fallback.");
        }

        this.hostName = (loadedHostName != null)
                ? loadedHostName
                : "http://localhost:8080";

        if (this.hostName.equals("http://localhost:8080")) {
            System.out.println("DEBUG: NtfyConnectionImpl running in test/fallback mode.");
        }
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean send(String message) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        System.out.println("Message sent successfully.");
                    } else {
                        System.err.println("Error while sending: " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Network issue: " + e.getMessage());
                    return null;
                });

        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s ->
                                mapper.readValue(s, NtfyMessageDto.class))
                        .filter(message -> message.event().equals("message"))
                        .peek(System.out::println)
                        .forEach(messageHandler));
    }
}