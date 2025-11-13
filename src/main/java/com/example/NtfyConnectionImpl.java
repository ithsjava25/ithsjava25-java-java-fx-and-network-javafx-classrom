package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        String hostFromEnv = dotenv.get("HOST_NAME");

        if (hostFromEnv == null || hostFromEnv.isBlank()) {
            throw new IllegalStateException("HOST_NAME saknas i .env-filen!");
        }

        hostName = hostFromEnv;
        System.out.println("HOST_NAME: " + hostName);
    }

    @Override
    public boolean send(String message) {
        if (message == null || message.isBlank()) {
            System.err.println("⚠️ Meddelandet är tomt, inget skickas.");
            return false;
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .header("Cache", "no")
                .build();

        CompletableFuture<HttpResponse<Void>> future = http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding());

        future.exceptionally(ex -> {
            System.err.println("❌ Fel vid sändning: " + ex.getMessage());
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
                .thenAccept(response -> response.body().forEach(line -> {
                    try {
                        NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                        if ("message".equals(msg.event())) {
                            messageHandler.accept(msg);
                        }
                    } catch (JsonProcessingException e) {
                        System.err.println("⚠️ JSON parsing error: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("❌ Network error while receiving messages: " + ex.getMessage());
                    return null;
                });
    }
}
