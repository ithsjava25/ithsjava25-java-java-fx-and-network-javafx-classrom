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

    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessages();
    }

    public HelloModel(NtfyConnection connection) {
        this.connection = Objects.requireNonNull(connection);
        receiveMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public void sendMessage(String text) {
        if (!connection.send(text)) showError("Kunde inte skicka meddelandet");
    }

    public boolean sendFile(File file) {
        if (file == null || !file.exists()) return false;
        try {
            boolean ok = connection.sendFile(file);
            if (!ok) showError("Kunde inte skicka filen");
            return ok;
        } catch (Exception e) {
            showError("Fel vid filöverföring: " + e.getMessage());
            return false;
        }
    }

    public void receiveMessages() {
        connection.receive(msg -> Platform.runLater(() -> {
            messages.add(msg);
            if (msg.hasAttachment()) {
                try {
                    saveAttachmentAutomatically(msg);
                } catch (IOException e) {
                    showError("Fel vid nedladdning av fil: " + e.getMessage());
                }
            }
        }));
    }

    public Image loadImageFromUrl(NtfyMessageDto item, int width, int height) {
        if (item.getAttachmentUrl() == null) return null;
        try (InputStream in = new URL(item.getAttachmentUrl()).openStream()) {
            return new Image(in, width, height, true, true);
        } catch (IOException e) {
            showError("Fel vid laddning av bild: " + e.getMessage());
            return null;
        }
    }

    private void saveAttachmentAutomatically(NtfyMessageDto item) throws IOException {
        if (item.getAttachmentUrl() == null) return;

        File downloads = new File("downloads");
        if (!downloads.exists()) downloads.mkdirs();

        File dest = new File(downloads, item.getAttachmentName());
        downloadFile(item.getAttachmentUrl(), dest);
    }

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

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void showInfo(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
