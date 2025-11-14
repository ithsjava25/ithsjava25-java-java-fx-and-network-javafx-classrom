package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String HOSTNAME;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SimpleStringProperty topic = new SimpleStringProperty("JUV25D"); //Default value simplifies code

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        HOSTNAME = Objects.requireNonNull(dotenv.get("HOSTNAME"));
    }

    @Override
    public boolean send(String message) {
        if (message == null) return false; if(message.isBlank()) return false; //only send messages that isn't null and has  text


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOSTNAME + "/" + topic.getValue()))
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        try {
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Error sending message");
            return false;
        } catch (InterruptedException e) {
            System.err.println("Interrupted sending message");
            return false;
        }
        return true;
    }

    /**
     * Converts a file to bytes and sends it with correct header
     * @param attachment File to be sent
     */
    public void sendFile(File attachment){

        try {
            byte[] attachmentAsBytes = Files.readAllBytes(Paths.get(attachment.getAbsolutePath()));

            String contentType = Files.probeContentType(attachment.toPath());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HOSTNAME + "/" + topic.getValue()))
                    .header("Content-Type", contentType)
                    .header("Filename", attachment.getName())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(attachmentAsBytes))
                    .build();


            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Error sending attachment");
        } catch (InterruptedException e) {
            System.err.println("Interrupted sending attachment");
        }
    }

    @Override
    public void recieve(Consumer<NtfyMessage> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOSTNAME + "/" + topic.getValue() + "/json?since=all"))
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> objectMapper.readValue(s, NtfyMessage.class))
                        .filter(m -> m.event().equals("message"))
                        .forEach(messageHandler));
    }

    @Override
    public void changeTopic() {
        Dialog dialog = new Dialog();
        dialog.setTitle("Add Topic");

        ButtonType addTopicButton = new ButtonType("Add Topic", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addTopicButton, ButtonType.CANCEL);

        TextField newTopic = new TextField();
        newTopic.setPromptText("Topic");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(newTopic, 0, 0);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(() -> newTopic.requestFocus());

        dialog.setResultConverter(pressedButton -> {
            if (pressedButton == addTopicButton) {
                if(!newTopic.getText().isBlank()){
                    topic.setValue(newTopic.getText().trim());
                    receive();
                }
            }
            return null;
        });

        dialog.show();
    }
}
