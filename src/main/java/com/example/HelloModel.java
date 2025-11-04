package com.example;


import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final Dotenv dotenv = Dotenv.load();
    private final String hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    private final HttpClient client = HttpClient.newBuilder().build();
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    private final ObjectMapper  objectMapper = new ObjectMapper();

    private final String clientId = java.util.UUID.randomUUID().toString();

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }
    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void sendMessage(String topic, String message) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hostName + "/" + topic))
                .header("Title",clientId)
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> System.out.println("Sent: " + response.statusCode()))
                .exceptionally(ex -> {
                    System.out.println("Error sending message: " + ex.getMessage());
                    return null;
                });
    }

    public void receiveMessage(String topic, Consumer<String> errorHandler) {
        String url = hostName + "/" + topic + "/json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAcceptAsync(response -> {
                    try (var reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(response.body()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isBlank()) continue;

                            try {
                                NtfyMessageDto msg = objectMapper.readValue(line, NtfyMessageDto.class);
                                if (!"message".equals(msg.event()) || msg.message() == null || msg.message().isBlank()) {
                                    return;
                                }
                                if (clientId.equals(msg.topic())) {
                                    return;
                                }
                                javafx.application.Platform.runLater(() -> messages.add(msg));
                            } catch (Exception parseEx) {
                                javafx.application.Platform.runLater(() ->
                                        errorHandler.accept("Fel vid tolkning av meddelande: " + parseEx.getMessage()));
                            }
                        }

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() ->
                                errorHandler.accept("Fel vid mottagning: " + e.getMessage()));
                    }
                })
                .exceptionally(ex -> {
                    javafx.application.Platform.runLater(() ->
                            errorHandler.accept("Kunde inte ansluta till servern: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }
}