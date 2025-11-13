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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final String HOSTNAME;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<NtfyMessage> messageHistory = FXCollections.observableArrayList();
    private final ObservableList<Object> formatedMessages = FXCollections.observableArrayList();
    
    private SimpleStringProperty topic = new SimpleStringProperty("JUV25D");


    public HelloModel() {
        Dotenv dotenv = Dotenv.load();
        HOSTNAME = Objects.requireNonNull(dotenv.get("HOSTNAME"));

    }


    public ObservableList<Object> getFormatedMessages() {
        return formatedMessages;
    }

    public ObservableList<NtfyMessage> getMessageHistory() {
        return messageHistory;
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


    /**
     * Sends a string as message
     * @param message String to send
     */
    public void sendMessage(String message) {
        if(message.isBlank()) return; //only send messages that has text

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOSTNAME + "/" + getTopic()))
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        try {
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Error sending message");
        } catch (InterruptedException e) {
            System.err.println("Interrupted sending message");
        }
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
                    .uri(URI.create(HOSTNAME + "/" + getTopic()))
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

    /**
     * Clears the display before opening a new connection to a topic and displaying it
     */
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


    /**
     * Adds the message to internal message history and the values to be displayed formated in the display list
     * @param message received message from NTFY server
     */
    public void logMessage(NtfyMessage message) {
        messageHistory.add(message);

        if(message.attachment() != null) {
            try {
                URL url = new URL(message.attachment().get("url"));

                if(Pattern.matches("^image\\/\\w+",message.attachment().get("type"))) {//check if the attachment is an image
                    ImageView image = new ImageView(new Image(url.toExternalForm()));
                    image.setPreserveRatio(true);
                    image.setFitHeight ( 250 );
                    image.setFitWidth ( 250 );

                    formatedMessages.addFirst(image);
                }

                else{
                    formatedMessages.addFirst(url);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        Date timeStamp = new Date(message.time()*1000);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String stringMessage = dateFormat.format(timeStamp) + " : " + message.message();
        formatedMessages.addFirst(stringMessage);
    }

    /**
     * Opens as dialog with text input
     * If a value is entered and the add button pressed, change the topic per the input
     */
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

}

