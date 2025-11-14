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
        if (item == null || !item.hasAttachment()) return;

        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(item.getAttachmentName());
        File destination = chooser.showSaveDialog(null); // Du kan sätta Stage om du vill

        if (destination != null) {
            new Thread(() -> {
                try (InputStream in = new URL(item.getAttachmentUrl()).openStream();
                     FileOutputStream out = new FileOutputStream(destination)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }

                    Platform.runLater(() -> showInfo("Filen sparad: " + destination.getAbsolutePath()));
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
