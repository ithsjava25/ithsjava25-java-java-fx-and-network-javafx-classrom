package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

//För tester
    private final NtfyConnection connection;
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {

        this.connection = connection;
        receiveMessage();
    }

    //getter från private
    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }
    //test
    public String getMessageToSend() {
        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }
    public void sendMessage(String message) {

        messageToSend.set(message);
        connection.send(messageToSend.get());

    }

    public void receiveMessage() {

        connection.receive(m -> Platform.runLater(() -> messages.add(m)));

    }
}


