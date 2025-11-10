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
                .header("Cache", "no")
                .uri(URI.create(hostName + "/mytopic"))
                .build();

        return http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> System.out.println("Message sent!"))
                .exceptionally(e -> {
                    System.out.println("Error sending message");
                    return null;
                });
    }
//            // Todo: handle long blocking send request to not freeze the java FX thread
//            //1. Use thread send message?
//            //2. Use async?

        @Override
        public void receive (Consumer < NtfyMessageDto > messageHandler) {
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

