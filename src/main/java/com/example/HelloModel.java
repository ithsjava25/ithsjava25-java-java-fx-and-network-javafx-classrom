package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.UUID;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final String clientId = UUID.randomUUID().toString();

    // För att undvika att visa eget bildmeddelande
    private long lastSentImageTime = 0;

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessages(); // ✅ Startar mottagning direkt
    }

    public String getClientId() {
        return clientId;
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public String getMessageToSend() {
        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        this.messageToSend.set(message);
    }

    // 🟢 Skicka textmeddelande
    public void sendMessage(String messageText) {
        if (messageText == null || messageText.isBlank()) return;

        // Skicka JSON till ntfy
        String jsonPayload = String.format("{\"clientId\":\"%s\",\"message\":\"%s\"}", clientId, messageText);
        connection.send(jsonPayload);

        // Lägg till direkt i GUI:t
        addMessageSafely(new NtfyMessageDto(
                UUID.randomUUID().toString(),
                System.currentTimeMillis() / 1000,
                "message",
                "MartinsTopic",
                messageText
        ));
    }

    // 🟢 Skicka bild
    public boolean sendImage(File imageFile) {
        lastSentImageTime = System.currentTimeMillis(); // 👈 Kom ihåg när vi skickade bilden
        return connection.sendImage(imageFile, clientId);
    }

    // 🟢 Mottagning
    private void receiveMessages() {
        connection.receive(dto -> {
            try {
                String raw = dto.message();

                // 🔹 Bildmeddelande från ntfy
                if (raw != null && raw.contains("You received a file:")) {
                    long now = System.currentTimeMillis();

                    // Om det är mycket nära i tid efter vi skickat → ignorera
                    if (now - lastSentImageTime < 3000) {
                        System.out.println("🟡 Ignorerar eget bildmeddelande (inom 3 sekunder)");
                        return;
                    }

                    String fileName = raw.replaceAll(".*file: ([^\\\"]+).*", "$1").trim();
                    addMessageSafely(new NtfyMessageDto(
                            dto.id(), dto.time(), dto.event(), dto.topic(),
                            "[📷 Bild mottagen: " + fileName + "]"
                    ));
                    return;
                }

                // 🔹 Textmeddelanden med clientId
                if (raw != null && raw.contains("{\"clientId\":")) {
                    String receivedClientId = raw.replaceAll(".*\"clientId\":\"([^\"]+)\".*", "$1");

                    if (receivedClientId.equals(clientId)) {
                        System.out.println("🟡 Ignorerar eget textmeddelande");
                        return;
                    }

                    String text = raw.replaceAll(".*\"message\":\"([^\"]+)\".*", "$1");
                    addMessageSafely(new NtfyMessageDto(
                            dto.id(), dto.time(), dto.event(), dto.topic(), text
                    ));
                    return;
                }

                // 🔹 Allt annat (t.ex. systemhändelser)
                addMessageSafely(dto);

            } catch (Exception e) {
                e.printStackTrace();
                addMessageSafely(dto);
            }
        });
    }

    // 🟢 Lägg till meddelande på rätt tråd
    private void addMessageSafely(NtfyMessageDto msg) {
        if (Platform.isFxApplicationThread()) {
            messages.add(msg);
        } else {
            try {
                Platform.runLater(() -> messages.add(msg));
            } catch (IllegalStateException e) {
                messages.add(msg);
            }
        }
    }
}
