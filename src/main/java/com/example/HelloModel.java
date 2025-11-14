package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;

public class HelloModel {
    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() { return messages; }

    public void sendMessage(String text) {
        boolean success = connection.send(text);
        if (!success) showError("Kunde inte skicka meddelandet");
    }

    public boolean sendFile(File file) {
        return connection.sendFile(file);
    }

    public void receiveMessages() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }

    public Image loadImageFromUrl(NtfyMessageDto item, int width, int height) {
        try (InputStream in = new URL(item.getAttachmentUrl()).openStream()) {
            return new Image(in, width, height, true, true);
        } catch (Exception e) {
            showError("Fel vid laddning av bild: " + e.getMessage());
            return null;
        }
    }

    public void saveAttachment(NtfyMessageDto item) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(item.getAttachmentName());
        File dest = chooser.showSaveDialog(null);
        if (dest != null) {
            new Thread(() -> {
                try (InputStream in = new URL(item.getAttachmentUrl()).openStream();
                     FileOutputStream out = new FileOutputStream(dest)) {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
                    Platform.runLater(() -> showInfo("Fil sparad: " + dest.getAbsolutePath()));
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Fel vid nedladdning: " + e.getMessage()));
                }
            }).start();
        }
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
