package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    private final NtfyConnection connection;
    private final StringProperty messageToSend = new SimpleStringProperty("");
    private final StringProperty currentTopic = new SimpleStringProperty("mytopic");
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final ObjectProperty<File> fileToSend = new SimpleObjectProperty<>(null); // Ny egenskap för filbilaga

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        connection.connect(currentTopic.get(), this::receiveMessage);
    }

    public String getGreeting() {
        return "Skicka meddelande";
    }

    // NY METOD: Hanterar att köra koden på JavaFX-tråden ELLER direkt i testmiljö
    private static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            // JavaFX toolkit not initialized (e.g., unit tests or CI without graphics): run inline
            task.run();
        }
    }


    public void sendMessage() {
        String message = messageToSend.get();
        if (message != null && !message.trim().isEmpty()) {
            connection.send(message, currentTopic.get());
            messageToSend.set(""); // Rensa meddelandefältet efter skickning
        }
    }

    // Argumentlös metod, som controlleren använder för att skicka den bifogade filen
    public void sendFile() {
        File file = fileToSend.get();
        if (file != null) {
            // Vi använder den specifika sendFile-metoden för att säkerställa att Content-Type och Filename headers sätts
            connection.sendFile(file, currentTopic.get());
            fileToSend.set(null); // Rensa filbilagan efter skickning
        }
    }

    /*
    // Denna metod används inte längre i applikationslogiken och tas bort
    public void sendFile(File file) {
        if (file != null) {
            try {
                // Denna metod ska skicka filen och *inte* rensa fileToSend-propertyn,
                // eftersom den främst används av tester för att simulera en skickning direkt.
                connection.sendFile(file, currentTopic.get());
            } catch (FileNotFoundException e) {
                System.err.println("Fel: Filen hittades inte: " + file.getAbsolutePath());
            }
        }
    }
    */


    // Används av HelloController för att hämta filbilagan
    public ObjectProperty<File> fileToSendProperty() {
        return fileToSend;
    }

    // Används av HelloController för att ställa in filbilagan
    public void setFileToSend(File file) {
        this.fileToSend.set(file);
    }

    private void receiveMessage(NtfyMessageDto message) {
        // ANVÄNDER runOnFx FÖR ATT SÄKRA ATT UPPDATERINGEN SKER PÅ RÄTT TRÅD (eller direkt i testmiljö)
        runOnFx(() -> messages.add(message));
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public StringProperty currentTopicProperty() {
        return currentTopic;
    }

    public void reconnectToTopic(String newTopic) {
        if (!currentTopic.get().equals(newTopic)) {
            // connection.disconnect(currentTopic.get()); // Förutsätter att disconnect implementeras i NtfyConnection
            currentTopic.set(newTopic);

            // Säkerställ att rensningen sker på FX-tråden om vi kör i en FX-miljö
            runOnFx(messages::clear);

            connection.connect(newTopic, this::receiveMessage);
        }
    }
}
