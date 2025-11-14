package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class HelloModel {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpClient client = HttpClient.newHttpClient();
    private final String topic;
    private final String backendUrl;

    /** Standardkonstruktor som läser från .env */
    public HelloModel() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.backendUrl = dotenv.get("BACKEND_URL", System.getenv("BACKEND_URL"));
        this.topic = dotenv.get("TOPIC", System.getenv("TOPIC"));
        if (backendUrl == null || topic == null) {
            throw new IllegalStateException("BACKEND_URL eller TOPIC saknas i .env");
        }
    }

    /** Alternativ konstruktor för tester */
    HelloModel(String topic, String backendUrl) {
        if (backendUrl == null || backendUrl.isBlank()) {
            throw new IllegalArgumentException("backendUrl must not be null/blank");
        }
        this.backendUrl = backendUrl;
        this.topic = topic;
    }

    public void sendMessage(String message) {
        String sender = "[Eric Chat App]";
        String fullMessage = sender + " " + message;

        String json = "{\"message\": \"" + fullMessage.replace("\"", "\\\"") + "\"}";
        String url = backendUrl + "/" + topic;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 300) {
                        System.err.println("⚠️ Misslyckades att skicka: " + response.statusCode() + " - " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("⚠️ Nätverksfel vid sendMessage: " + ex.getMessage());
                    return null;
                });
    }

    public void sendFile(File file) {
        try {
            String url = backendUrl + "/" + topic;
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "application/octet-stream";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", contentType)
                    .header("X-Filename", file.getName())
                    .header("Title", "File: " + file.getName())
                    .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 300) {
                            System.err.println("⚠️ Filupload misslyckades: " + response.statusCode() + " - " + response.body());
                        } else {
                            System.out.println("✅ Fil skickad: " + file.getName());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("⚠️ Nätverksfel vid sendFile: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("⚠️ Kunde inte läsa/skicka fil: " + e.getMessage());
        }
    }

    public CompletableFuture<Void> listen(MessageHandler handler) {
        String url = backendUrl + "/" + topic + "/json";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body().forEach(line -> {
                    String parsed = parseIncomingLine(line);
                    if (!parsed.isEmpty()) {
                        handler.onMessage(parsed);
                        System.out.println("📩 Meddelande: " + parsed);
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("⚠️ Nätverksfel vid listen: " + ex.getMessage());
                    return null;
                });
    }

    String parseIncomingLine(String line) {
        try {
            JsonNode outer = mapper.readTree(line);
            String raw = outer.path("message").asText("");
            if (raw.isEmpty()) return "";

            String clean = raw.startsWith("{")
                    ? mapper.readTree(raw).path("message").asText(raw)
                    : raw;

            if (!clean.contains("[Eric Chat App]") && !clean.contains("[Javafx-chat]")) {
                clean = "[Javafx-chat] " + clean;
            }

            return "💬 " + clean;
        } catch (Exception e) {
            System.err.println("⚠️ Kunde inte tolka rad: " + line + " | " + e.getMessage());
            return "";
        }
    }

    public interface MessageHandler {
        void onMessage(String message);
    }
}
