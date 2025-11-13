package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
    public CompletableFuture<Void> send(String message) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache-Control", "no")
                .uri(URI.create(hostName + "/mytopic"))
                .build();

        return http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> System.out.println("Message sent!"))
                .exceptionally(e -> {
                    System.out.println("Error sending message");
                    return null;
                });
    }

        @Override
        public void receive (Consumer < NtfyMessageDto > messageHandler) {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(hostName + "/mytopic/json"))
                    .build();

            http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> response.body()
                            .map(s -> {
                                try {
                                    return mapper.readValue(s, NtfyMessageDto.class);
                                } catch (Exception e) {
                                    System.out.println("Failed to parse message");
                                    return null;
                                }
                            })
                            .filter(message -> message !=null && message.event().equals("message"))
                            .peek(System.out::println)
                            .forEach(messageHandler));
        }
    }

