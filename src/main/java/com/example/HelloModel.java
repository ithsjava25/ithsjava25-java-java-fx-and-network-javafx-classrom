package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final String HOSTNAME;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<NtfyMessage> messageHistory = FXCollections.observableArrayList();
    private final ObservableList<String> formatedMessages = FXCollections.observableArrayList();
    
    private SimpleStringProperty topic = new SimpleStringProperty("");
    
    public HelloModel() {
        Dotenv dotenv = Dotenv.load();
        HOSTNAME = Objects.requireNonNull(dotenv.get("HOSTNAME"));
    }

    public ObservableList<NtfyMessage> getMessageHistory() {
        return messageHistory;
    }
    

    public void sendMessage(String message) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(HOSTNAME + "/" + getTopic()))
                .build();

        try {
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Error sending message");
        } catch (InterruptedException e) {
            System.err.println("Interrupted sending message");
        }

    }

    public void receiveMessage() {
        formatedMessages.clear();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(HOSTNAME + "/" + getTopic() + "/json?since=all"))
                .build();

        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> objectMapper.readValue(s, NtfyMessage.class))
                        .filter(m -> m.event().equals("message"))
                        .forEach((m -> Platform.runLater(() -> logMessage(m)))));
    }

    public void logMessage(NtfyMessage message) {
        messageHistory.add(message);;

        Date timeStamp = new Date(message.time()*1000);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String stringMessage = dateFormat.format(timeStamp) + " : " + message.message();
        formatedMessages.add(stringMessage);
    }

    public String getTopic() {
        return topic.get();
    }

    public SimpleStringProperty topicProperty() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic.set(topic);
    }

    public void changeTopic() {
        Dialog dialog = new Dialog();
        dialog.setTitle("Add Topic");

        ButtonType addTopicButton = new ButtonType("Add Topic", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addTopicButton, ButtonType.CANCEL);

        TextField topic = new TextField();
        topic.setPromptText("Topic");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(topic, 0, 0);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(() -> topic.requestFocus());

        dialog.setResultConverter(pressedButton -> {
            if (pressedButton == addTopicButton) {
                    setTopic(topic.getText().trim());
                    receiveMessage();
            }
            return null;
        });

        dialog.show();
    }

    public ObservableList<String> getFormatedMessages() {
        return formatedMessages;
    }
}

