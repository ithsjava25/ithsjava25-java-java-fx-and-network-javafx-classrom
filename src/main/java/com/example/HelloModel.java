package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {


    private final NtfyConnection connection;
    private final StringProperty currentTopic = new SimpleStringProperty("general");

    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    private static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }


    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        connection.connect(currentTopic.get(), this::receiveMessageHandler);

    }

    public ObservableList<NtfyMessageDto> getMessages() {

        return messages;
    }

    public StringProperty currentTopicProperty() {
        return currentTopic;
    }

    public void setCurrentTopic(String topic) {
        currentTopic.set(topic);
    }

    public String getMessageToSend() {

        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {

        return messageToSend;
    }

    public void setMessageToSend(String message) {

        messageToSend.set(message);
    }


    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Send message";
    }

    public void reconnectToTopic(String newTopic) {

        if (newTopic.equals(connection.getTopic())) {
            return;
        }

        setCurrentTopic(newTopic);

        runOnFx(messages::clear);

        connection.connect(newTopic, this::receiveMessageHandler);
    }

    private void receiveMessageHandler(NtfyMessageDto message) {
        runOnFx(() -> messages.add(message));
    }


    public void sendMessage() {
        String message = messageToSend.get();

        if (message != null && !message.trim().isEmpty()) {
            connection.send(message, currentTopic.get());
        }
    }



    public void sendFile(File file) {
        connection.sendFile(file, currentTopic.get());
    }

}
