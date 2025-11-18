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

    private final ObservableList<NtfyMessage> messages = FXCollections.observableArrayList();
    private final BooleanProperty connected = new SimpleBooleanProperty(false);
    private boolean isTesting = false;  // <-- Lägg till denna rad

    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    public ObservableList<NtfyMessage> getMessages() {
        return messages;
    }


    public ReadOnlyBooleanProperty connectedProperty() {
        return connected;
    }

    public void setTesting(boolean testing) {  // <-- Lägg till denna metod
        this.isTesting = testing;
    }

    // Ersätt den gamla runOnFX-metoden med denna:
    private void runOnFX(Runnable task) {
        if (isTesting) {  // <-- Ny logik för tester
            task.run();
        } else if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            try {
                Platform.runLater(task);
            } catch (IllegalThreadStateException notInitialized) {
                task.run();  // Fallback för enhetstester (om JavaFX inte är initierat)
            }
        }

    }

    public void addMessage(NtfyMessage message) {
        runOnFX(() -> messages.add(message));
    }

    public void setConnected(boolean connected) {
        runOnFX(() -> this.connected.set(connected));
    }


}
