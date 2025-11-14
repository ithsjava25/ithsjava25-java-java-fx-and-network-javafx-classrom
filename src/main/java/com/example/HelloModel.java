package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.NtfyConnection;

import java.io.*;
import java.net.URL;

public class HelloModel {
    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() { return messages; }

    public void sendMessage(String text) {
        boolean success = connection.send(text);
        if (!success) showError("Kunde inte skicka meddelandet");
    }

    public boolean sendFile(File file) { return connection.sendFile(file); }

    public void receiveMessages() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }

    public void downloadAttachment(NtfyMessageDto item, File destination) {
        new Thread(() -> {
            try (InputStream in = new URL(item.getAttachmentUrl()).openStream();
                 FileOutputStream out = new FileOutputStream(destination)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) out.write(buf, 0, r);

                Platform.runLater(() -> showInfo("Fil sparad: " + destination.getAbsolutePath()));

            } catch (Exception e) { Platform.runLater(() -> showError("Fel vid nedladdning: " + e.getMessage())); }
        }).start();
    }

    public Image loadImageFromUrl(NtfyMessageDto item) {
        try (InputStream in = new URL(item.getAttachmentUrl()).openStream()) {
            return new Image(in, 100, 100, true, true);
        } catch (Exception e) { return null; }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
