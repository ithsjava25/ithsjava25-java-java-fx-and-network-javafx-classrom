package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    public String getMessageToSend() {
        return messageToSend.get();
    }

    public String getGreeting() {
        return "Welcome to NTFY chat";
    }

    public void sendMessage(String enteredText) {
        // Create local message immediately for instant feedback with current timestamp
        NtfyMessageDto localMessage = new NtfyMessageDto(
                "local-" + System.currentTimeMillis(),
                System.currentTimeMillis() / 1000, // Convert to seconds (Unix timestamp)
                "message",
                "mytopic",
                enteredText,
                null,
                null
        );

        Platform.runLater(() -> {
            messages.add(localMessage);
            System.out.println("Message sent locally: " + enteredText + " at " + localMessage.getFormattedDateTime());
        });

        // Then send to server
        boolean success = connection.send(enteredText);
        if (!success) {
            System.err.println("Failed to send message to server: " + enteredText);
        }
    }

    public void receiveMessage() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }

    public boolean sendFile(File file) {
        return connection.sendFile(file);
    }

    public boolean sendFileFromUrl(String url) {
        System.out.println("sendFileFromUrl not implemented yet for URL: " + url);
        return false;
    }

    public void downloadAttachment(NtfyMessageDto item, File destination) {
        new Thread(() -> {
            try {
                String attachmentUrl = item.getAttachmentUrl();
                if (attachmentUrl != null) {
                    URL url = new URL(attachmentUrl);
                    try (InputStream in = url.openStream();
                         FileOutputStream out = new FileOutputStream(destination)) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }

                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Fil nedladdad");
                            alert.setHeaderText(null);
                            alert.setContentText("Fil sparad som: " + destination.getAbsolutePath());
                            alert.showAndWait();
                        });
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fel vid nedladdning");
                    alert.setHeaderText(null);
                    alert.setContentText("Kunde inte ladda ner filen: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    public void previewImage(NtfyMessageDto item) {
        new Thread(() -> {
            try {
                String attachmentUrl = item.getAttachmentUrl();
                if (attachmentUrl != null) {
                    URL url = new URL(attachmentUrl);
                    try (InputStream in = url.openStream()) {
                        Image image = new Image(in);

                        Platform.runLater(() -> {
                            // Skapa ett nytt fönster för att visa bilden
                            Stage imageStage = new Stage();
                            ImageView imageView = new ImageView(image);
                            imageView.setPreserveRatio(true);
                            imageView.setFitWidth(600);

                            ScrollPane scrollPane = new ScrollPane(imageView);
                            scrollPane.setFitToWidth(true);
                            scrollPane.setFitToHeight(true);

                            javafx.scene.Scene scene = new javafx.scene.Scene(scrollPane, 800, 600);
                            imageStage.setTitle("Förhandsvisning: " + item.getAttachmentName());
                            imageStage.setScene(scene);
                            imageStage.show();
                        });
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Fel vid förhandsvisning");
                    alert.setHeaderText(null);
                    alert.setContentText("Kunde inte visa bilden: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
}