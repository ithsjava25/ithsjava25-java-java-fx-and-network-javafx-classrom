package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyHttpClient implements ChatNetworkClient{
    private final HttpClient http;  //Singleton (återanvänds för alla anrop)
    private final ObjectMapper mapper;
    
    public NtfyHttpClient() {
        this.http = HttpClient.newHttpClient(); //Implicit Singleton
        this.mapper = new ObjectMapper();
    }

    @Override
    public void send(String baseUrl, NtfyMessage message) throws Exception{
        //Builder Pattern för HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(
                        String.format("{\"topic\":\"%s\",\"message\":\"%s\"}", message.topic(), message.message())
                ))
                .header("Content-Type", "application/json")
                .uri(URI.create(baseUrl + "/" + message.topic()))
                .build();

        //Synkront anrop (blockerande)
        HttpResponse<Void> response = http.send(
                request,
                HttpResponse.BodyHandlers.discarding()
        );
        //validera statuskoden
        if (response.statusCode() >= 400) {
            throw new IOException("Fel vid sändning: HTTP " + response.statusCode());
        }
    }

    public Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> messageHandler) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(baseUrl + "/" + topic + "/json"))
                .build();

        //Asynkront anrop (icke-blockerande)
        CompletableFuture<Void> future = http.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofLines()
        ).thenAccept(response -> {
            response.body()
                    .map(line-> {
                        try {
                            return mapper.readValue(line, NtfyMessage.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(message -> message.event().equals("message"))
                    .forEach(message -> {
                        //Uppdatera UI på JavaFX-tråden
                        Platform.runLater(() -> messageHandler.accept(message));
                    });
        }).exceptionally(error -> {
            System.err.println("Fel vid prenumeration: " + error.getMessage());
            return null;
        });

        //Command Pattern: Returnera ett Subscription-objekt
        return new Subscription() {
            @Override
            public void close() throws IOException {
                future.cancel(true);
            }

            @Override
            public boolean isOpen() {
                return !future.isDone();
            }
        };
    }
}
