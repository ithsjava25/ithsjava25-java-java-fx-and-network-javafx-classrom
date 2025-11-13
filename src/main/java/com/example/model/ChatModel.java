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

    /**
     * Constructs a ChatModel, initializing the network client, selecting the NTFY base URL,
     * setting the topic to "myChatTopic", and establishing the subscription.
     *
     * <p>The base URL is chosen in this order of precedence: the system property
     * "NTFY_BASE_URL", the environment variable "NTFY_BASE_URL", and finally the default
     * "http://localhost:8080". This constructor prints the chosen URL and calls {@code connect()}
     * to begin receiving messages.
     */
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

    /**
     * Provides the observable list of chat messages.
     *
     * @return the observable list of NtfyMessage instances; changes to this list are observable and reflect the model's current messages
     */
    public ObservableList<NtfyMessage> getMessages() {
        return messages;
    }

    /**
     * Establishes a subscription to the configured Ntfy topic and begins receiving messages.
     *
     * Incoming messages are appended to the model's observable messages list on the JavaFX
     * application thread. Subscription errors are written to standard error.
     */
    public void connect() {
        this.subscription = networkClient.subscribe(
                baseUrl,
                topic,
                msg -> Platform.runLater(() -> messages.add(msg)),
                error -> System.err.println("Error: " + error.getMessage())
        );
    }

    /**
     * Publish a text message to the configured chat topic.
     *
     * @param text the message content to send
     * @throws Exception if sending the message fails
     */
    public void sendMessage(String text) throws Exception {
        NtfyMessage message = new NtfyMessage(topic, text);
        networkClient.send(baseUrl, message);
    }

    /**
     * Sends the given file to the configured chat topic and posts a chat message describing the file.
     *
     * Validates that the file exists, uploads it via the network client, then sends a message containing
     * the file name and a human-readable file size.
     *
     * @param file the file to send; must be non-null and exist on disk
     * @throws IllegalArgumentException if {@code file} is null or does not exist
     * @throws Exception if the upload or subsequent message send fails
     */
    public void sendFile(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File not found");
        }

        networkClient.sendFile(baseUrl, topic, file);

        String fileMessage = "📎 " + file.getName() + " (" + formatFileSize(file.length()) + ")";
        sendMessage(fileMessage);
    }

    /**
     * Format a byte count into a human-readable string using B, KB, MB, or GB.
     *
     * @param size the size in bytes
     * @return a formatted size string (e.g., "512 B", "1.5 KB", "2.0 MB", or "3.2 GB")
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}