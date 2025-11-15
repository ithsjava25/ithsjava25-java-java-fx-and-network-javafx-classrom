package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.util.Objects;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    /**
     * Default constructor that creates a new NtfyConnection instance
     */
    public HelloModel() {
        this.connection = new NtfyConnectionImpl();receiveMessages();}

    /**
     * Constructor that accepts a custom NtfyConnection instance
     * @param connection the NtfyConnection implementation to use for messaging
     * @throws NullPointerException if connection is null
     */
    public HelloModel(NtfyConnection connection) {
        this.connection = Objects.requireNonNull(connection);
        receiveMessages();
    }

    /**
     * Gets the observable list of messages for UI binding
     * @return ObservableList containing all received and sent messages
     */
    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /**
     * Sends a text message through the Ntfy connection
     * @param text the message text to send
     */
    public void sendMessage(String text) {
        if (!connection.send(text)) showError("Could not send message");
    }

    /**
     * Sends a file through the Ntfy connection
     * @param file the file to send, must exist and not be null
     * @return true if file was sent successfully, false otherwise
     */
    public boolean sendFile(File file) {
        if (file == null || !file.exists()) return false;
        try {
            boolean ok = connection.sendFile(file);
            if (!ok) showError("Could not send file");
            return ok;
        } catch (Exception e) {
            showError("Error sending file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Starts receiving messages from the Ntfy connection
     * Automatically filters system events and handles attachments
     */
    public void receiveMessages() {
        connection.receive(msg -> Platform.runLater(() -> {
            // Filtrera bort system-event
            if (!"message".equals(msg.event())) return;

            messages.add(msg);
            if (msg.hasAttachment()) {
                try {
                    saveAttachmentAutomatically(msg);
                } catch (IOException e) {
                    showError("Error downloading file: " + e.getMessage());
                }
            }
        }));
    }

    /**
     * Automatically saves attachments from messages to the downloads folder
     * @param item the message DTO containing attachment information
     * @throws IOException if the download or file creation fails
     */
    private void saveAttachmentAutomatically(NtfyMessageDto item) throws IOException {
        if (item.getAttachmentUrl() == null) return;
        File downloads = new File("downloads");
        if (!downloads.exists()) downloads.mkdirs();
        File dest = new File(downloads, item.getAttachmentName());
        downloadFile(item.getAttachmentUrl(), dest);
    }

    /**
     * Downloads a file from a URL to a local destination
     * @param urlString the URL string of the file to download
     * @param dest the destination file where the download will be saved
     * @throws IOException if the network connection or file writing fails
     */
    private void downloadFile(String urlString, File dest) throws IOException {
        try (InputStream in = new URL(urlString).openStream();
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Shows an error alert dialog on the JavaFX application thread
     * @param msg the error message to display in the alert
     */
    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
