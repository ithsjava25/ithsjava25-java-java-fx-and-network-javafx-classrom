package com.example;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final String hostName;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final ObjectMapper  mapper = new  ObjectMapper();

    public HelloModel() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
        receiveMessage();
    }



    private final String clientId = java.util.UUID.randomUUID().toString();

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }
    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void sendMessage() {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString("Hello! This is a test."))
                .uri(URI.create(hostName + "/MartinsTopic"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> System.out.println("Sent: " + response.statusCode()))
                .exceptionally(ex -> {
                    System.out.println("Error sending message: " + ex.getMessage());
                    return null;
                });
    }

    public void receiveMessage() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/MartinsTopic/json"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines()).thenAccept(response -> response.body()
                        .map(s-> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .filter(message->message.event().equals("message"))
                        .peek(System.out::println)
                        .forEach(s->Platform.runLater(()->messages.add(s))));
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