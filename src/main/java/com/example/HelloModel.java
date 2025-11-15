package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.URL;
import java.util.Objects;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private ErrorHandler errorHandler;

    // Interface för felhantering (för testbarhet)
    public interface ErrorHandler {
        void showError(String message);
    }

    // Default error handler som använder JavaFX
    private static class DefaultErrorHandler implements ErrorHandler {
        @Override
        public void showError(String msg) {
            if (Platform.isFxApplicationThread()) {
                showAlert(msg);
            } else {
                Platform.runLater(() -> showAlert(msg));
            }
        }

        private void showAlert(String msg) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }

    // Test error handler som kör koden direkt (för tester)
    public static class TestErrorHandler implements ErrorHandler {
        @Override
        public void showError(String message) {
            // Skriv bara till console i tester - inga JavaFX alerts
            System.err.println("ERROR: " + message);
        }
    }

    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        this.errorHandler = new DefaultErrorHandler();
        receiveMessages();
    }

    public HelloModel(NtfyConnection connection) {
        this.connection = Objects.requireNonNull(connection);
        this.errorHandler = new DefaultErrorHandler();
        receiveMessages();
    }

    // Konstruktor för tester
    public HelloModel(NtfyConnection connection, ErrorHandler errorHandler) {
        this.connection = Objects.requireNonNull(connection);
        this.errorHandler = Objects.requireNonNull(errorHandler);
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
        connection.receive(msg -> {
            // Använd Platform.runLater bara om det behövs
            if (Platform.isFxApplicationThread()) {
                handleIncomingMessage(msg);
            } else {
                try {
                    Platform.runLater(() -> handleIncomingMessage(msg));
                } catch (IllegalStateException e) {
                    // JavaFX Platform är inte startad (i tester) - hantera direkt
                    handleIncomingMessage(msg);
                }
            }
        });
    }

    private void handleIncomingMessage(NtfyMessageDto msg) {
        // Filtrera bort system-event
        if (!"message".equals(msg.event())) return;

        messages.add(msg);
        if (msg.hasAttachment()) {
            try {
                saveAttachmentAutomatically(msg);
            } catch (IOException e) {
                showError("Fel vid nedladdning av fil: " + e.getMessage());
            }
        }
    }

    private void saveAttachmentAutomatically(NtfyMessageDto item) throws IOException {
        if (item.getAttachmentUrl() == null) return;
        File downloads = new File("downloads");
        if (!downloads.exists() && !downloads.mkdirs()) {
            throw new IOException("Kunde inte skapa downloads mapp");
        }
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
        errorHandler.showError(msg);
    }

    // Metod för att sätta error handler (användbar i tester)
    void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler);
    }
}