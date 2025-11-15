package com.example;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    //Nya fält för chat-funktionalitet
    private final ObservableList<NtfyMessage> messages = FXCollections.observableArrayList();
    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    //Getter för meddelandelistan
    public ObservableList<NtfyMessage> getMessages() {
        return messages;
    }

    //Getter för anslutningstillstånd
    public ReadOnlyBooleanProperty connectedProperty() {
        return connected;
    }

    //Metod för att lägga till meddelande
    public void addMessage(NtfyMessage message) {
        runOnFX(() -> messages.add(message));
    }

    //Metod för att uppdatera anslutningstillstånd
    public void setConnected(boolean connected) {
        runOnFX(() -> this.connected.set(connected));
    }

    //Dispatcher Utility
    private static void runOnFX(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalThreadStateException notInitialized) {
            task.run(); //Fallback för enhetstester
        }
    }
}
