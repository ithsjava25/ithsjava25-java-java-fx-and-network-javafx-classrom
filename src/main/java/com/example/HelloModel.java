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

    // Argumentlös metod, som controlleren använder för att skicka den bifogade filen
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

    // KORRIGERAD: Denna metod måste ta en String för att matcha testet!
    public void setMessageToSend(String message) {
        this.messageToSend.set(message);
    }
}
