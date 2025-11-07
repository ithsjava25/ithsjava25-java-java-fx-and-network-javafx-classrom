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
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/MartinsTopic/json"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines()).thenAccept(response -> response.body()
                .map(s-> {
                    try {
                        return mapper.readValue(s, NtfyMessageDto.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(message->message.event().equals("message"))
                .peek(System.out::println)
                .forEach(messageHandler));
    }

}
