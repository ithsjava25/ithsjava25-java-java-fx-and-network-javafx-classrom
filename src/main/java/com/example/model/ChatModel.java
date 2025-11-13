package com.example.model;

import com.example.network.ChatNetworkClient;
import com.example.network.NtfyHttpClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

public class ChatModel {

    private final ObservableList<NtfyMessage> messages = FXCollections.observableArrayList();
    private final ChatNetworkClient networkClient;
    private ChatNetworkClient.Subscription subscription;
    private final String baseUrl;
    private final String topic;

    public ChatModel() {
        this.networkClient = new NtfyHttpClient();

        String url = System.getProperty("NTFY_BASE_URL");
        if (url == null || url.isBlank()) {
            url = System.getenv("NTFY_BASE_URL");
        }
        if (url == null || url.isBlank()) {
            url = "http://localhost:8080";
        }

        this.baseUrl = url;
        this.topic = "myChatTopic";

        System.out.println("Using NTFY URL: " + this.baseUrl);
        connect();
    }

    public ObservableList<NtfyMessage> getMessages() {
        return messages;
    }

    public void connect() {
        this.subscription = networkClient.subscribe(
                baseUrl,
                topic,
                msg -> Platform.runLater(() -> messages.add(msg)),
                error -> System.err.println("Error: " + error.getMessage())
        );
    }

    public void sendMessage(String text) throws Exception {
        NtfyMessage message = new NtfyMessage(topic, text);
        networkClient.send(baseUrl, message);
    }

    public void sendFile(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File not found");
        }

        networkClient.sendFile(baseUrl, topic, file);

        String fileMessage = "📎 " + file.getName() + " (" + formatFileSize(file.length()) + ")";
        sendMessage(fileMessage);
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}