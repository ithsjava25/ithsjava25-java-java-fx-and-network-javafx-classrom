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

    private final ObservableList<NtfyMessageDto> messages= FXCollections.observableArrayList();
    private final StringProperty messageToSend= new SimpleStringProperty();


    public HelloModel(NtfyConnection connection){
        this.connection=connection;
        receiveMessage();

    }

    public ObservableList<NtfyMessageDto> getMessages(){

        return messages;
    }

    public String getMessageToSend() {

        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {

        return messageToSend;
    }

    public void setMessageToSend(String message){

        messageToSend.set(message);
    }

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Send message";
    }


    public void sendMessage() {

        connection.send(messageToSend.get());
    }



    public void receiveMessage(){

        connection.receive(m-> Platform.runLater(()->messages.add(m)));
    }


    public void sendFile(File file) throws FileNotFoundException {
        connection.sendFile(file);
    }
}
