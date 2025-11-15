package com.example.chat;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ChatModel {

    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String ntfyUrl;

    public ChatModel() {
        // Hämta backend-url från env variable
        String url = System.getenv("NTFY_URL");
        if (url == null || url.isBlank()) {
            url = "http://localhost:8080/topic/test"; // fallback
        }
        ntfyUrl = url;
        startListening();
    }

    public ObservableList<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        if (message != null && !message.isBlank()) {
            messages.add("Me: " + message);
            sendToBackend(message);
        }
    }

    private void sendToBackend(String message) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ntfyUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8))
                    .header("Content-Type", "text/plain")
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ntfyUrl + "/events"))
                        .build();

                client.send(request, HttpResponse.BodyHandlers.ofLines())
                        .body().forEach(line -> Platform.runLater(() -> {
                            if (!line.isBlank()) messages.add("Other: " + line);
                        }));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}



