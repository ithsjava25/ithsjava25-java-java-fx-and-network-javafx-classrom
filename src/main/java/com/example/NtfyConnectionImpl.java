package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {
    private final String hostName;
    private final HttpClient http = HttpClient.newHttpClient();
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
        //Send message to client - HTTP meddelande
        String inputMessage = Objects.requireNonNull(message);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(inputMessage))
                .header("Cache", "no")
                .uri(URI.create(hostName + "/catChat"))
                .build();
        try {
            var response = http.send(httpRequest, HttpResponse.BodyHandlers.discarding());
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
            System.out.println("Sending message interrupted");
        }
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/catChat/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(s -> mapper.readValue(s, NtfyMessageDto.class))
                        .filter(message -> message.event().equals("message"))
                        .peek(System.out::println)
                        .forEach(messageHandler));
    }
}
