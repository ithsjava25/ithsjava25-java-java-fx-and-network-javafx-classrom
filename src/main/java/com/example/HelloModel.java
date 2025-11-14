package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;



public class HelloModel {
    private final NtfyConnection connection;

    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }


    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public void addMessage(NtfyMessageDto message) {
        messages.add(message);
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


    public void sendMessage() {
        setMessageToSend("Hello, World!");
        connection.send(messageToSend.get());


    }

    public void receiveMessage() {
        connection.recieve(m-> Platform.runLater(()-> messages.add(m)));
    }

}

