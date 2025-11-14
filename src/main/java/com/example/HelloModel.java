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
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

public class HelloModel {
    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final String clientId = UUID.randomUUID().toString();
    public static final String DEFAULT_TOPIC = "MartinsTopic";

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

    public void sendMessage(String messageText) {
        if (messageText == null || messageText.isBlank()) return;

        // Skicka ren text
        connection.send(messageText);

        // Lägg till i GUI direkt
        addMessageSafely(new NtfyMessageDto(
                UUID.randomUUID().toString(),
                System.currentTimeMillis() / 1000,
                "message",
                DEFAULT_TOPIC,
                messageText,
                null,
                null
        ));
    }

    public boolean sendImage(File imageFile) {
        try {
            String imageUrl = uploadToLocalServer(imageFile);
            String markdownMessage = String.format("Bild: %s\n![Bild](%s)", imageFile.getName(), imageUrl);
            connection.send(markdownMessage);
            addMessageSafely(new NtfyMessageDto(UUID.randomUUID().toString(),
                    System.currentTimeMillis() / 1000,
                    "message",
                    DEFAULT_TOPIC,
                    null,
                    null,
                    imageUrl));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void receiveMessages() {
        connection.receive(dto -> {
            String msg = dto.message();
            if (msg == null || msg.isBlank()) return;

            // Ignorera egna meddelanden
            if (msg.contains("\"clientId\":\"" + clientId + "\"")) {
                return;
            }

            // Identifiera Markdown-bild via enkel substring-sökning
            int marker = msg.indexOf("![Bild](");
            if (marker != -1) {
                int open = msg.indexOf('(', marker);
                int close = msg.indexOf(')', open + 1);

                if (open != -1 && close != -1) {
                    String url = msg.substring(open + 1, close);
                    addMessageSafely(new NtfyMessageDto(
                            dto.id(),
                            dto.time(),
                            dto.event(),
                            dto.topic(),
                            null,
                            null,
                            url
                    ));
                    return;
                }
            }

            // Ren text
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

    protected String uploadToLocalServer(File imageFile) throws IOException {
        URL url = new URL("http://localhost:8081/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        try (OutputStream os = conn.getOutputStream()) {
            Files.copy(imageFile.toPath(), os);
        }

        int status = conn.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IOException("Upload failed with status " + status);
        }

        try (InputStream in = conn.getInputStream()) {
            return new String(in.readAllBytes());
        } finally {
            conn.disconnect();
        }
    }



}