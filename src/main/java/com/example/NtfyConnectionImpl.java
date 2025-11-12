package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    private String currentTopic = "general";
    private CompletableFuture<Void> currentSubscription = CompletableFuture.completedFuture(null);

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }
    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    @Override
    public String getTopic() {
        return currentTopic;
    }

    @Override
    public void connect(String newTopic, Consumer<NtfyMessageDto> messageHandler) {

        currentSubscription.cancel(true);
        System.out.println("Cancelled subscription on " + currentTopic);


        currentTopic = newTopic;

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/" + currentTopic + "/json"))
                .build();


        currentSubscription = http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (JsonProcessingException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(message -> message.event().equals("message"))
                        .forEach(messageHandler))
                .exceptionally(e -> {

                    if (!(e instanceof java.util.concurrent.CancellationException)) {
                        System.out.println("Failure in receiving on: " + currentTopic + ": " + e.getMessage());
                    }
                    return null;
                });
    }


    @Override
    public boolean send(String message, String topic) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache", "no")
                .uri(URI.create(hostName + "/" + topic))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .exceptionally(e -> {
                    System.out.println("Error sending message to " + topic + ": " + e.getMessage());
                    return null;
                });

        return true;

//        try {
//            //Todo: handle long blocking send requests to not freeze the JavaFX thread
//            //1. Use thread send message?
//            //2. Use async?
//            var response = http.send(httpRequest, HttpResponse.BodyHandlers.discarding());
//            return true;
//        } catch (IOException e) {
//            System.out.println("Error sending message");
//        } catch (InterruptedException e) {
//            System.out.println("Interruped sending message");
//        }
//        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        connect(currentTopic, messageHandler);
//        HttpRequest httpRequest = HttpRequest.newBuilder()
//                .GET()
//                .uri(URI.create(hostName + "/mytopic/json"))
//                .build();
//
//        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
//                .thenAccept(response -> response.body()
//                        .map(s -> {
//                            try {
//                                return mapper.readValue(s, NtfyMessageDto.class);
//                            } catch (JsonProcessingException e) {
//                                return null;
//                            }
//                        })
//                        .filter(message -> message.event().equals("message"))
//                        .peek(System.out::println)
//                        .forEach(messageHandler));
    }

    @Override
    public boolean sendFile(File file, String topic) {
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not valid: " + file.getAbsolutePath());
            return false;
        }

        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .header("Content-Type", determineContentType(file.getName()))
                    .header("Filename", file.getName())
                    .uri(URI.create(hostName + "/" + topic))
                    .build();

            http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(e -> {
                        System.out.println("Failure in asynchrone file transfer to " + topic + ":" + e.getMessage());
                        return null;
                    });
            return true;

        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
            return false;
        }


    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
