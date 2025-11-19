package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;

    private final Consumer<Runnable> uiExecutor;

    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection, Consumer<Runnable> uiExecutor) {
        this.connection = connection;
        this.uiExecutor = uiExecutor;
        receiveMessage();
    }

    public HelloModel(NtfyConnection connection) {
        this(connection, Platform::runLater);
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
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

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }
    public void sendMessage() {
        String msg = messageToSend.get();
        if (msg != null && !msg.isBlank()) {
            connection.sendMessage(msg);
        }
    }

    public void receiveMessage() {
        connection.receiveMessage(m -> uiExecutor.accept(() -> messages.add(m)));
    }
}

