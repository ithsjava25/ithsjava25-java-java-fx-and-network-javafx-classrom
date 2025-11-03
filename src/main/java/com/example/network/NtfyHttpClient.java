package com.example.network;

import com.example.model.NtfyMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyHttpClient implements ChatNetworkClient {

    private final HttpClient http;
    private final ObjectMapper objectMapper;

    public NtfyHttpClient() {
        this.http = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void send(String baseUrl, NtfyMessage message) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> onMessage, Consumer<Throwable> onError) {
        String url = (baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl) + "/" + topic + "/json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        CompletableFuture<Void> future = http.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.trim().isEmpty()) {
                                NtfyMessage message = objectMapper.readValue(line.trim(), NtfyMessage.class);
                                if ("message".equals(message.event())) {
                                    onMessage.accept(message);
                                }
                            }
                        }
                    } catch (Exception e) {
                        onError.accept(e);
                    }
                })
                .exceptionally(error -> {
                    onError.accept(error);
                    return null;
                });

        return () -> future.cancel(true);
    }
}