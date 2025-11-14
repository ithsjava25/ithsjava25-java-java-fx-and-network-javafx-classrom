package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;

    private final ObservableList<NtfyMessage> messageHistory = FXCollections.observableArrayList();
    private final ObservableList<Object> formatedMessages = FXCollections.observableArrayList();


    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
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
        connection.send(message);
    }

    /**
     * Clears the display before opening a new connection to a topic and displaying it
     */
    public void receiveMessage() {
        formatedMessages.clear();
        connection.recieve(m -> Platform.runLater(() -> logMessage(m)));
    }


    /**
     * Adds the message to internal message history and the values to be displayed formated in the display list
     * If the message is an attachment it displays it as an image or hyperlink depending on content
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
                    Hyperlink hyperlink = new Hyperlink(url.toExternalForm());
                    formatedMessages.addFirst(hyperlink);
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

    }
}