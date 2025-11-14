package com.example;


import javafx.application.Platform;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;

import java.io.File;

import java.util.*;


/**
 * Model layer: encapsulates application data and business logic.
 * Manages the current message, topic, list of received messages, and connection to Ntfy.
 */
public class HelloModel {
    private final NtfyConnection connection;
    private final StringProperty messageToSend = new SimpleStringProperty("");
    private final StringProperty currentTopic = new SimpleStringProperty("mytopic");
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final ObjectProperty<File> fileToSend = new SimpleObjectProperty<>(null); // Ny egenskap för filbilaga

    /**
     * Initializes the model and establishes connection to the specified Ntfy server.
     * @param connection The Ntfy connection implementation to use (e.g., NtfyConnectionImpl or a Spy).
     */
    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        connection.connect(currentTopic.get(), this::receiveMessage);
    }
    /**
     * Returns a standard greeting string.
     * @return The greeting string.
     */
    public String getGreeting() {
        return "Skicka meddelande";
    }

    // NY METOD: Hanterar att köra koden på JavaFX-tråden ELLER direkt i testmiljö
    private static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }

    /**
     * Sends the current text message to the Ntfy server via the connection.
     * The message is added to the local list before sending.
     */
    public void sendMessage() {
        String message = messageToSend.get();
        if (message != null && !message.trim().isEmpty()) {

            // 1. Skapa den lokala DTO:n (med isLocal = true)
            NtfyMessageDto localMessage = new NtfyMessageDto(
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis() / 1000L,
                    "message",
                    currentTopic.get(),
                    message.trim(),
                    true // Markera som lokalt skickat
            );

            // 2. Lägg till i listan (UI-uppdatering) PÅ RÄTT TRÅD
            runOnFx(() -> messages.add(localMessage));

            // 3. Skicka meddelandet via anslutningen
            connection.send(message, currentTopic.get());

            // 4. Rensa meddelandefältet efter skickning
            messageToSend.set("");
        }
    }

    /**
     * Sends the currently attached file to the Ntfy server.
     * The file's name and a temporary message are added to the local list before sending.
     * The file attachment is cleared after sending.
     */
    public void sendFile() {
        File file = fileToSend.get();
        if (file != null) {

            // 1. Skapa den lokala DTO:n för fil (ofta med tom message)
            NtfyMessageDto localFileMessage = new NtfyMessageDto(
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis() / 1000L,
                    "file", // Använd "file" event om det är en fil
                    currentTopic.get(),
                    "Fil skickad: " + file.getName(), // Detta meddelande visas bara i logik, CellFactory hanterar visning
                    true
            );

            // 2. Lägg till i listan (UI-uppdatering) PÅ RÄTT TRÅD
            runOnFx(() -> messages.add(localFileMessage));

            // 3. Skicka filen
            connection.sendFile(file, currentTopic.get());

            // 4. Rensa filbilagan efter skickning
            fileToSend.set(null);
        }
    }

    /**
     * Property for the file currently attached to be sent.
     * @return The ObjectProperty containing the File object, or null if no file is attached.
     */
    public ObjectProperty<File> fileToSendProperty() {
        return fileToSend;
    }

    /**
     * Sets the file to be sent with the next message.
     * @param file The file to attach. Set to null to clear the attachment.
     */
    public void setFileToSend(File file) {
        this.fileToSend.set(file);
    }


    /**
     * Handles an incoming message from the Ntfy connection and adds it to the message list on the FX thread.
     * @param message The received NtfyMessageDto.
     */
    private void receiveMessage(NtfyMessageDto message) {

        runOnFx(() -> messages.add(message));
    }

    /**
     * Returns the observable list of messages received and sent.
     * @return The ObservableList of NtfyMessageDto objects.
     */
    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /**
     * Property for the message currently being composed to send.
     * @return The StringProperty holding the message content.
     */
    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    /**
     * Property for the current Ntfy topic being subscribed to.
     * @return The StringProperty holding the current topic name.
     */
    public StringProperty currentTopicProperty() {
        return currentTopic;
    }


    /**
     * Sets the message content to send. Used by the controller for bidirectional binding.
     * @param message The new message content.
     */
    public void setMessageToSend(String message) {
        this.messageToSend.set(message);
    }
}
