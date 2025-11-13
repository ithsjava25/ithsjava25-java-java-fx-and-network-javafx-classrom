package com.example;

import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class NtfyMessageListCell extends ListCell<NtfyMessageDto> {

    @Override
    protected void updateItem(NtfyMessageDto message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setText(null);
            setGraphic(null);
        } else {
            VBox container = new VBox(5);

            // Visa meddelandetext
            String messageText = message.message() != null ? message.message() : "(Inget meddelande)";
            Label textLabel = new Label(messageText);

            // Visa filinformation om det finns en bilaga
            if (message.attachment() != null) {
                NtfyMessageDto.Attachment attachment = message.attachment();

                System.out.println("📎 Mottagen bilaga: " + attachment.name());
                System.out.println("🔗 URL: " + attachment.url());
                System.out.println("📁 Typ: " + attachment.type());

                // Kolla om det är en bild
                boolean isImage = attachment.name().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$") ||
                        (attachment.type() != null && attachment.type().startsWith("image/"));

                if (isImage) {
                    // Visa bild med förhandsvisning
                    try {
                        ImageView imageView = new ImageView();
                        imageView.setFitHeight(150);
                        imageView.setFitWidth(150);
                        imageView.setPreserveRatio(true);

                        // Ladda bilden asynkront
                        Image image = new Image(attachment.url(), true);
                        imageView.setImage(image);

                        // Lägg till klickbar länk under bilden
                        Hyperlink imageLink = new Hyperlink("🖼️ " + attachment.name() + " (" + formatFileSize(attachment.size()) + ")");
                        imageLink.setOnAction(e -> openFile(attachment.url()));

                        container.getChildren().addAll(textLabel, imageView, imageLink);

                    } catch (Exception e) {
                        System.err.println("❌ Kunde inte ladda bild: " + e.getMessage());
                        // Fallback till vanlig filvisning
                        showFileLink(container, textLabel, attachment);
                    }
                } else {
                    // Visa vanlig fillänk
                    showFileLink(container, textLabel, attachment);
                }
            } else {
                container.getChildren().add(textLabel);
            }

            setGraphic(container);
        }
    }

    private void showFileLink(VBox container, Label textLabel, NtfyMessageDto.Attachment attachment) {
        Hyperlink fileLink = new Hyperlink("📎 " + attachment.name() + " (" + formatFileSize(attachment.size()) + ")");
        fileLink.setStyle("-fx-text-fill: blue; -fx-underline: true;");
        fileLink.setOnAction(e -> openFile(attachment.url()));

        // Visa filtyp
        Label fileType = new Label("Typ: " + (attachment.type() != null ? attachment.type() : "Okänd"));
        fileType.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        container.getChildren().addAll(textLabel, fileLink, fileType);
    }

    private void openFile(String url) {
        try {
            System.out.println("🔗 Öppnar: " + url);
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception ex) {
            System.err.println("❌ Kunde inte öppna fil: " + ex.getMessage());
            showError("Kunde inte öppna fil: " + ex.getMessage() + "\n\nURL: " + url);
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return (size / (1024 * 1024)) + " MB";
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Fel");
        alert.setHeaderText("Kunde inte öppna fil");
        alert.setContentText(message);
        alert.showAndWait();
    }
}