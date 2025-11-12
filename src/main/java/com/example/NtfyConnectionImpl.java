package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();
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
    public CompletableFuture<Boolean> send(String message) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache", "no")
                .uri(URI.create(hostName + "/mytopic"))
                .build();

        return http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .thenApply( response -> {
                    System.out.println("Your message was sent");
                    return true;
            })
                .exceptionally(e -> {
                    System.out.println("Error sending the message: " + e.getMessage());
                    return false;
            });
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .flatMap(line -> {
                            if (line.isBlank()) return java.util.stream.Stream.empty();
                            try {
                                NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                                return java.util.stream.Stream.of(msg);
                            } catch (Exception e) {
                                System.err.println("Kunde inte tolka: " + line);
                                return java.util.stream.Stream.empty();
                            }
                        })
                        .filter(msg -> "message".equals(msg.event()))
                        .peek(msg -> System.out.println("Mottaget: " + msg.message()))
                        .forEach(messageHandler));
    }
}