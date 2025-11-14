package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.beans.property.SimpleStringProperty;
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

    public NtfyConnectionImpl(String HOSTNAME) {
        this.HOSTNAME = HOSTNAME;
    }

    @Override
    public SimpleStringProperty topicProperty() {
        return topic;
    }


    @Override
    public String getTopic() {
        return topic.getValue();
    }


    @Override
    public void setTopic(String topic) {
        if (topic != null && !topic.isEmpty()) {
            this.topic.setValue(topic);
        }
    }

    /**
     * Sends a string as message
     *
     * @param message String to send
     */
    @Override
    public boolean send(String message) {
        if (message == null || message.isBlank()) return false; //only send messages that isn't null and has  text


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HOSTNAME + "/" + topic.getValue()))
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        try {
            HttpResponse response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                System.out.println("Error sending message " + response.statusCode());
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error sending message");
        } catch (InterruptedException e) {
            System.err.println("Interrupted sending message");
        }
        return false;
    }


    /**
     * Converts a file to bytes and sends it with correct header
     *
     * @param attachment File to be sent
     */
    public boolean sendFile(File attachment) {
        if (attachment == null || !attachment.exists()) return false;

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

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            } else {
                System.out.println("Error sending attachment " + response.statusCode());
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error sending attachment");
        } catch (InterruptedException e) {
            System.err.println("Interrupted sending attachment");
        }
        return false;
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
}


