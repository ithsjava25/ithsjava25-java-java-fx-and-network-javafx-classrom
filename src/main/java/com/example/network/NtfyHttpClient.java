package com.example.network;

import com.example.model.NtfyMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
                .uri(URI.create(ensureNoTrailingSlash(baseUrl)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to send message. Status: " + response.statusCode());
        }
    }

    @Override
    public void sendFile(String baseUrl, String topic, File file) throws IOException, InterruptedException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getName());
        }

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String url = ensureNoTrailingSlash(baseUrl) + "/" + topic;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Filename", file.getName())
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String errorBody = response.body();
            throw new IOException("Failed to send file. Status: " + response.statusCode() + ", Error: " + errorBody);
        }

        System.out.println("✅ File uploaded successfully: " + file.getName());
    }

    @Override
    public Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> onMessage, Consumer<Throwable> onError) {
        String url = ensureNoTrailingSlash(baseUrl) + "/" + topic + "/json";

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

    private String ensureNoTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}