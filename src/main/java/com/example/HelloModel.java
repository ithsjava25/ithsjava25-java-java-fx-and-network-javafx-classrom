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
import java.util.Objects;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final String hostName;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    private boolean senderMe=false;

    public HelloModel() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    public void sendMessage(String text) {

        senderMe=true;

        HttpRequest httpRequest= HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("Hello world"))
                .uri(URI.create(hostName + "/mytopic"))
                .build();
        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> {
                    System.out.println("Error sending message: " + ex.getMessage());
                    return null;
                });
        try {
            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
            System.out.println("Interrupted sending request");
        }

    }

    public void receiveMessage(){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (Exception e) {
                                System.err.println("Failed to parse message: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(msg -> "message".equals(msg.event()))
                        .forEach(msg->{
                            if (senderMe) {
                                senderMe=false;
                                return;
                            }
                        Platform.runLater(() -> messages.add(msg));
                        }));
    }

}
