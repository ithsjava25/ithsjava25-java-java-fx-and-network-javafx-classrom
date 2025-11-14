package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http;
    private final String hostName;
    private final ObjectMapper mapper;

    public NtfyConnectionImpl() {
        this(HttpClient.newHttpClient(), new ObjectMapper(), loadHostNameFromEnv());
    }

    public NtfyConnectionImpl(String hostName) {
        this(HttpClient.newHttpClient(), new ObjectMapper(), hostName);
    }


    NtfyConnectionImpl(HttpClient http, ObjectMapper mapper, String hostName) {
        this.http = http;
        this.mapper = mapper;
        this.hostName = hostName;
    }

    private static String loadHostNameFromEnv() {
        Dotenv dotenv = Dotenv.load();
        return Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    @Override
    public boolean send(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        try {

            String jsonPayload = String.format("{\"message\":\"%s\"}", escapeJson(message.trim()));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .header("Content-Type", "application/json")
                    .header("Cache", "no-cache")
                    .uri(URI.create(hostName + "/mytopic"))
                    .build();


            http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            System.out.println("Message sent successfully");
                        } else {
                            System.out.println("Failed to send message. Status: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        System.out.println("Error sending message: " + ex.getMessage());
                        return null;
                    });

            return true;
        } catch (Exception e) {
            System.out.println("Error in send: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String base64Content = Base64.getEncoder().encodeToString(fileContent);


            String jsonPayload = String.format(
                    "{\"message\":\"File: %s\", \"file\":\"%s\", \"filename\":\"%s\"}",
                    escapeJson(file.getName()), base64Content, escapeJson(file.getName())
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .header("Content-Type", "application/json")
                    .header("Cache", "no-cache")
                    .uri(URI.create(hostName + "/mytopic"))
                    .build();

            http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            System.out.println("File sent successfully: " + file.getName());
                        } else {
                            System.out.println("Failed to send file. Status: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        System.out.println("Error sending file: " + ex.getMessage());
                        return null;
                    });

            return true;
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(hostName + "/mytopic/json"))
                    .build();

            http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            response.body()
                                    .map(s -> {
                                        try {
                                            return mapper.readValue(s, NtfyMessageDto.class);
                                        } catch (Exception e) {
                                            System.out.println("Error parsing message: " + e.getMessage());
                                            return null;
                                        }
                                    })
                                    .filter(Objects::nonNull)
                                    .filter(message -> "message".equals(message.event()))
                                    .forEach(messageHandler);
                        } else {
                            System.out.println("Failed to receive messages. Status: " + response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        System.out.println("Error receiving messages: " + ex.getMessage());
                        return null;  // Lade till return statement här
                    });
        } catch (Exception e) {
            System.out.println("Error in receive: " + e.getMessage());
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}