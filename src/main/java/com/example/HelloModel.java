package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final String HOSTNAME;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<NtfyMessage> messageHistory = FXCollections.observableArrayList();

    public HelloModel() {
        Dotenv dotenv = Dotenv.load();
        HOSTNAME = Objects.requireNonNull(dotenv.get("HOSTNAME"));
    }

    public ObservableList<NtfyMessage> getMessageHistory() {
        return messageHistory;
    }

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    public void sendMessage() {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("Hello World!"))
                .uri(URI.create(HOSTNAME + "/aTopic"))
                .build();

        try {
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Error sending message");
        } catch (InterruptedException e) {
            System.err.println("Interrupted sending message");
        }

    }

    public void receiveMessage() {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOSTNAME + "/aTopic/json"))
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> objectMapper.readValue(s, NtfyMessage.class))
                        .filter(m -> m.event().equals("message"))
                        .forEach((m -> Platform.runLater(() -> messageHistory.add(m)))));
    }
}

