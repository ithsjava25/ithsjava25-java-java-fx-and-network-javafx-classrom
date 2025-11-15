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

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        Platform.runLater(() -> messages.add("Error sending message: " + ex.getMessage()));
                        return null;
                    });
        } catch (Exception e) {
            messages.add("Error sending message: " + e.getMessage());
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(ntfyUrl + "/events"))
                            .GET()
                            .build();

                    client.send(request, HttpResponse.BodyHandlers.ofLines())
                            .body()
                            .forEach(line -> {
                                if (!line.isBlank() && !line.contains("data:")) return; // ignore non-data lines
                                String content = line.replaceFirst("data:", "").trim();
                                if (!content.isEmpty()) {
                                    Platform.runLater(() -> messages.add("Other: " + content));
                                }
                            });

                    Thread.sleep(1000); // poll every second
                }
            } catch (Exception e) {
                Platform.runLater(() -> messages.add("Error receiving messages: " + e.getMessage()));
            }
        }).start();
    }
}
