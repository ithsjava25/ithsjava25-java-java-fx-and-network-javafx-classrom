package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
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
    /**
     * Handles and returns a list of messages observed by JavaFX
     * Stores, changes and returns data. Remove?
     */
    //Lista som håller alla meddelanden
    //FXCollections.observableArrayList() = Nyckel som gör listan ändrings-bar och som JavaFX kan lyssna på
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    //Istället för String NtfyMessageDto
    private final String hostName;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public HelloModel() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));

    }

    //getter från private
    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    //tar in ett meddelande från controller och lägger till det i listan
    public void addMessages(NtfyMessageDto message) {
        messages.add(message);
    }

    public void sendMessage(String messageText) {
        //Send message to client - HTTP meddelande
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(messageText))
                .uri(URI.create(hostName + "/mytopic"))
                .build();
        try {
            var response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
            System.out.println("Sending message interrupted");
        }
    }

    public void receiveMessage() {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> mapper.readValue(s, NtfyMessageDto.class))
                        .peek(System.out::println)
                        .forEach(s ->
                                Platform.runLater(() -> messages.add(s))));
        //.exceptionally()
        //close
    }
}


