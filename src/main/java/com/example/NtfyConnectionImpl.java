package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        String host = dotenv.get("HOST_NAME");

        if (host == null || host.isEmpty()) {
            throw new IllegalStateException("HOST_NAME has to be defined");
        }
        this.hostName = host;
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean send (String message) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(hostName + "/mytopic"))
            .POST(HttpRequest.BodyPublishers.ofString(message))
            .build();

            http.send(request, HttpResponse.BodyHandlers.discarding());
            return true;

        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending message!");
            return false;
        }
    }

    @Override
    public void receive (Consumer<NtfyMessageDto> handler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(hostName + "/mytopic/json"))
                .GET()
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(line -> {
                            try {
                                return mapper.readValue(line, NtfyMessageDto.class);
                            } catch (JsonProcessingException e) {
                                return null;
                            }
                        })
                        .filter(msg -> msg != null && msg.event().equals("message"))
                        .forEach(handler));
    }
}
