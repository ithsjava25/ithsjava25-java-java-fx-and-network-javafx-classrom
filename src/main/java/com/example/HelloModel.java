package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final String clientId = java.util.UUID.randomUUID().toString();

    public HelloModel(NtfyConnection connection){
        this.connection = connection;
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {

        return messages;
    }

    public String getMessageToSend(){
        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {

        return messageToSend;
    }

    public void setMessageToSend(String message){
        this.messageToSend.set(message);
    }

    public void sendMessage() {

        connection.send(messageToSend.get());
//        HttpRequest request = HttpRequest.newBuilder()
//                .POST(HttpRequest.BodyPublishers.ofString("Hello! This is a test."))
//                .uri(URI.create(hostName + "/MartinsTopic"))
//                .build();
//
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenAccept(response -> System.out.println("Sent: " + response.statusCode()))
//                .exceptionally(ex -> {
//                    System.out.println("Error sending message: " + ex.getMessage());
//                    return null;
//                });
    }

    public void receiveMessage() {
        connection.receive(m -> Platform.runLater(()-> messages.add(m)));
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