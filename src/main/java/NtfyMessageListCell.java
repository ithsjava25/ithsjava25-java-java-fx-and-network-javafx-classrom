package com.example;

import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Hyperlink;

public class NtfyMessageListCell extends ListCell<NtfyMessageDto> {

    @Override
    protected void updateItem(NtfyMessageDto message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setText(null);
            setGraphic(null);
        } else {
            VBox container = new VBox(5);
            Label textLabel = new Label(message.message() != null ? message.message() : "");

            // Visa filinformation om det finns en bilaga
            if (message.attachment() != null) {
                NtfyMessageDto.Attachment attachment = message.attachment();
                Hyperlink fileLink = new Hyperlink("📎 " + attachment.name() + " (" + formatFileSize(attachment.size()) + ")");
                fileLink.setOnAction(e -> {
                    // Öppna filen i webbläsaren/systemets standardapp
                    try {
                        java.awt.Desktop.getDesktop().browse(java.net.URI.create(attachment.url()));
                    } catch (Exception ex) {
                        System.err.println("❌ Kunde inte öppna fil: " + ex.getMessage());
                    }
                });
                container.getChildren().addAll(textLabel, fileLink);
            } else {
                container.getChildren().add(textLabel);
            }

            setGraphic(container);
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return (size / (1024 * 1024)) + " MB";
    }
}