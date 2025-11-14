package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final String clientId = UUID.randomUUID().toString();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
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
        this.messageToSend.set(message);
    }

    // skickar text till ntfy
    public void sendMessage(String messageText) {
        if (messageText == null || messageText.isBlank()) return;

        // Skicka ren text
        connection.send(messageText);
    }

    // Skickar bild till lokal server och postar Markdown-länk till ntfy
    public boolean sendImage(File imageFile) {
        try {
            String imageUrl = uploadToLocalServer(imageFile);

            // Skickar Markdown-länk med filnamn som titel
            String markdownMessage = String.format(
                    "Bild: %s\n![Bild](%s)",
                    imageFile.getName(),
                    imageUrl
            );
            connection.send(markdownMessage);

            // Lägg till i GUI: endast bilden, ingen text
            addMessageSafely(new NtfyMessageDto(
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis() / 1000,
                    "message",
                    "MartinsTopic",
                    null,
                    null,
                    imageUrl
            ));

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // mottagning av meddelanden
    private void receiveMessages() {
        connection.receive(dto -> {
            if (dto.message() == null || dto.message().isBlank()) return;

            // ignorera egna meddelanden
            if (dto.message().contains("\"clientId\":\"" + clientId + "\"")) {
                return;
            }

            // Markdown-bild
            if (dto.message().startsWith("![Bild](") || dto.message().contains("\n![Bild](")) {
                String url = dto.message().replaceAll(".*!\\[Bild\\]\\(([^)]+)\\).*", "$1");
                addMessageSafely(new NtfyMessageDto(dto.id(), dto.time(), dto.event(), dto.topic(), null, null, url));
                return;
            }

            // Textmeddelande
            addMessageSafely(dto);
        });
    }

    private void addMessageSafely(NtfyMessageDto msg) {
        if (Platform.isFxApplicationThread()) {
            messages.add(msg);
        } else {
            Platform.runLater(() -> messages.add(msg));
        }
    }

    // laddar upp fil till lokal server och returnerar URL
    private String uploadToLocalServer(File imageFile) throws IOException {
        URL url = new URL("http://localhost:8081/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        try (OutputStream os = conn.getOutputStream()) {
            Files.copy(imageFile.toPath(), os);
        }

        String imageUrl = new String(conn.getInputStream().readAllBytes());
        conn.disconnect();
        return imageUrl;
    }
}
