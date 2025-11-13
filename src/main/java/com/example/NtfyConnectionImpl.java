package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"), "HOST_NAME must be set in .env file");
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean send(String message) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .uri(URI.create(hostName + "/mytopic"))
                    .build();

            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;

        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending message: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    response.body()
                            .forEach(line -> {
                                try {
                                    NtfyMessageDto message = mapper.readValue(line, NtfyMessageDto.class);
                                    if ("message".equals(message.event())) {
                                        messageHandler.accept(message);
                                    }
                                } catch (JsonProcessingException e) {
                                    System.err.println("Error parsing message: " + e.getMessage());
                                }
                            });
                })
                .exceptionally(throwable -> {
                    System.err.println("Error receiving messages: " + throwable.getMessage());
                    return null;
                });
    }

    @Override
    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            System.out.println("Filen är ogiltig eller saknas.");
            return false;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Content-Type", contentType)
                    .header("Filename", file.getName())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();

            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                System.out.println("Fel vid sändning av fil. Statuskod: " + response.statusCode());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error reading or sending file: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
}