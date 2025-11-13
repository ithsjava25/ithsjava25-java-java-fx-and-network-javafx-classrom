package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new  ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }


    @Override
    public boolean send(String message) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
               .uri(URI.create(hostName + "/MartinsTopic"))
               .build();

        try {
            var respone = client.send(request, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message.");
        } catch (InterruptedException e) {
            System.out.println("Interrupted sending message.");
        }
        return false;

    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        new Thread(() -> {
            while (true) {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(hostName + "/MartinsTopic/json"))
                        .build();

                try {
                    client.send(request, HttpResponse.BodyHandlers.ofLines())
                            .body()
                            .forEach(line -> {
                                try {
                                    NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                                    if ("message".equals(msg.event())) {
                                        Platform.runLater(() -> messageHandler.accept(msg));
                                    }
                                } catch (JsonProcessingException e) {
                                    // Ignorera tomma eller ogiltiga rader
                                }
                            });
                } catch (IOException | InterruptedException e) {
                    System.out.println("Disconnected from ntfy, reconnecting...");
                    try {
                        Thread.sleep(2000); // Vänta lite innan reconnect
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }, "NtfyReceiverThread").start();
    }


}
