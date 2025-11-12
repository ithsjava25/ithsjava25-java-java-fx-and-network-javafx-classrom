package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class HelloModel {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String topic;
    private final String backendUrl;

    public HelloModel(String topic) {
        this.topic = topic;
        this.backendUrl = System.getenv("BACKEND_URL");
        if (backendUrl == null) {
            throw new IllegalStateException("BACKEND_URL is not set!");
        }
    }

    public void sendMessage(String message) {
        String json = "{\"message\": \"" + message + "\"}";
        String url = backendUrl + "/" + topic;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<Void> listen(MessageHandler handler) {
        String url = backendUrl + "/" + topic + "/json";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body().forEach(line -> {
                    if (line.contains("\"message\"")) {
                        handler.onMessage(line);
                    }
                }));
    }

    public interface MessageHandler {
        void onMessage(String message);
    }
}
