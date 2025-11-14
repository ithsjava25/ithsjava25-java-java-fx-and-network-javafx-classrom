package com.example;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class HelloModel {
    private final NtfyConnection connection;
    private final javafx.collections.ObservableList<NtfyMessageDto> messages =
            javafx.collections.FXCollections.observableArrayList();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessages();
    }

    public javafx.collections.ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    // Skicka textmeddelande
    public void sendMessage(String text) {
        boolean success = connection.send(text);
        if (!success) showError("Kunde inte skicka meddelandet");
    }

    // Skicka fil
    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            showError("Filen finns inte");
            return false;
        }
        boolean ok = connection.sendFile(file);
        if (!ok) showError("Fel vid filuppladdning");
        return ok;
    }

    // Ta emot meddelanden från servern
    public void receiveMessages() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }

    // Ladda bild från URL
    public Image loadImageFromUrl(NtfyMessageDto item, int width, int height) {
        try (InputStream in = new URL(item.getAttachmentUrl()).openStream()) {
            return new Image(in, width, height, true, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Ladda bild för intern användning (standard 300x300)
    public Image loadImageFromUrl(NtfyMessageDto item) {
        return loadImageFromUrl(item, 300, 300);
    }

    // Ladda fil/attachment
    public void downloadAttachment(NtfyMessageDto item, File destination) {
        CompletableFuture.runAsync(() -> {
            try (InputStream in = new URL(item.getAttachmentUrl()).openStream();
                 FileOutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                Platform.runLater(() -> showInfo("Fil sparad: " + destination.getAbsolutePath()));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Fel vid nedladdning: " + e.getMessage()));
            }
        });
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fel");
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void showInfo(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
