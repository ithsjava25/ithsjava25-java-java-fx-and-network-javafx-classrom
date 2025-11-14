package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final String clientId = UUID.randomUUID().toString();
    public static final String DEFAULT_TOPIC = "MartinsTopic";

    private final boolean headless; // <-- För tester utan JavaFX

    // Konstruktor för GUI
    public HelloModel(NtfyConnection connection) {
        this(connection, false);
    }

    // Konstruktor för headless eller GUI
    public HelloModel(NtfyConnection connection, boolean headless) {
        if (connection == null) throw new IllegalArgumentException("Connection cannot be null");
        this.connection = connection;
        this.headless = headless;
        receiveMessages();
    }

    public String getClientId() {
        return clientId;
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    /**
     * Skickar ett textmeddelande via NtfyConnection och lägger till det i lokal lista.
     */
    public void sendMessage(String messageText) {
        if (messageText == null || messageText.isBlank()) return;

        connection.send(messageText);  // plain text
        addMessageSafely(new NtfyMessageDto(
                UUID.randomUUID().toString(),
                System.currentTimeMillis() / 1000,
                "message",
                DEFAULT_TOPIC,
                messageText,
                null,
                null,
                clientId
        ));
        messageToSend.set(""); // Clear bound property
    }

    /**
     * Skickar en bildfil via lokal uppladdning och notifikation.
     */
    public boolean sendImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) return false;
        try {
            String imageUrl = uploadToLocalServer(imageFile);

            // Skicka bild-notis via Ntfy
            connection.send("Bild: " + imageFile.getName() + "\n![Bild](" + imageUrl + ")");

            // Lägg till i lokal lista
            addMessageSafely(new NtfyMessageDto(
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis() / 1000,
                    "message",
                    DEFAULT_TOPIC,
                    null,
                    null,
                    imageUrl,
                    clientId
            ));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Startar mottagning av meddelanden från NtfyConnection.
     */
    private void receiveMessages() {
        connection.receive(dto -> {
            if (dto == null) return;

            // Ignorera egna meddelanden
            if (clientId.equals(dto.clientId())) return;

            if (dto.imageUrl() != null) {
                // Bild-meddelande
                addMessageSafely(new NtfyMessageDto(
                        dto.id(),
                        dto.time(),
                        dto.event(),
                        dto.topic(),
                        null,
                        null,
                        dto.imageUrl(),
                        dto.clientId()
                ));
            } else {
                // Text-meddelande
                addMessageSafely(dto);
            }
        });
    }

    /**
     * Lägger till meddelande i ObservableList på korrekt tråd.
     * Headless: läggs direkt, annars via Platform.runLater om ej på FX-tråden.
     */
    private void addMessageSafely(NtfyMessageDto msg) {
        if (headless) {
            messages.add(msg);
        } else if (Platform.isFxApplicationThread()) {
            messages.add(msg);
        } else {
            Platform.runLater(() -> messages.add(msg));
        }
    }

    /**
     * Laddar upp en fil till lokal server och returnerar URL som sträng.
     */
    protected String uploadToLocalServer(File imageFile) throws IOException {
        URL url = new URL("http://localhost:8081/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(5_000);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        // Bestäm MIME-typ
        String contentType = Files.probeContentType(imageFile.toPath());
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IOException("Unsupported image type: " + contentType);
        }
        conn.setRequestProperty("Content-Type", contentType);

        try (OutputStream os = conn.getOutputStream()) {
            Files.copy(imageFile.toPath(), os);
        }

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            conn.disconnect();
            throw new IOException("Upload failed with HTTP status: " + status);
        }

        try (InputStream in = conn.getInputStream()) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } finally {
            conn.disconnect();
        }
    }

}
