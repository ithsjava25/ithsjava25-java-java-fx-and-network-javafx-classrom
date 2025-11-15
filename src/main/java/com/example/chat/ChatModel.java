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
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ntfyUrl))
                        .POST(HttpRequest.BodyPublishers.ofString(message, StandardCharsets.UTF_8))
                        .header("Content-Type", "text/plain")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    Platform.runLater(() -> messages.add("Error sending message: " + response.statusCode()));
                }
            } catch (Exception e) {
                Platform.runLater(() -> messages.add("Error sending message: " + e.getMessage()));
            }
        }).start();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(ntfyUrl + "/events"))
                            .build();

                    client.send(request, HttpResponse.BodyHandlers.ofLines())
                            .body()
                            .forEach(line -> {
                                if (line != null && !line.isBlank()) {
                                    // Ta bort "data:" om det finns
                                    String content = line.contains("data:") ? line.replaceFirst("data:", "").trim() : line;
                                    if (!content.isEmpty()) {
                                        Platform.runLater(() -> messages.add("Other: " + content));
                                    }
                                }
                            });

                    Thread.sleep(1000); // poll varje sekund
                }
            } catch (Exception e) {
                Platform.runLater(() -> messages.add("Error receiving messages: " + e.getMessage()));
            }
        }).start();
    }
}

