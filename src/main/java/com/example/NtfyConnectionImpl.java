package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection{

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean send(String message) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Cache", "no")
                .uri(URI.create(hostName + "/mytopic"))
                .build();
        try {
            //Todo: handle long blocking send requests to not freeze the JavaFX thread
            //1. Use thread send message?
            //2. Use async?
            var response = http.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
            System.out.println("Interruped sending message");
        }
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> {
                            try {
                                return mapper.readValue(s, NtfyMessageDto.class);
                            } catch (JsonProcessingException e) {
                                return null;
                            }
                        })
                        .filter(message -> message.event().equals("message"))
                        .peek(System.out::println)
                        .forEach(messageHandler));
    }

    @Override
    public boolean sendFile(File file) throws FileNotFoundException {
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not valid: " + file.getAbsolutePath());
            return false;
        }
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                .header("Content-Type", determineContentType(file.getName())) //
                .header("Filename", file.getName())
                .uri(URI.create(hostName + "/mytopic"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
                .exceptionally(e -> {
                    System.out.println("Failure in asynchrone file transfer " + e.getMessage());
                    return null;
                });

        return true;
    }

    private String determineContentType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
