package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicBoolean;
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
                .POST(HttpRequest.BodyPublishers.ofString(message.message()))
                .header("Content-Type", "application/json")
                .uri(URI.create(baseUrl + "/" + message.topic()))
                .build();

        //Debug-loggning
        System.out.println("Skickar till: " + baseUrl + "/" + message.topic());
        System.out.println("Meddelande: " + message.message());

        //Synkront anrop (blockerande)
        HttpResponse<Void> response = http.send(
                request,
                HttpResponse.BodyHandlers.discarding()
        );
        //validera statuskoden
        if (response.statusCode() >= 400) {
            throw new IOException("Fel vid sändning: HTTP " + response.statusCode());
        }
        System.out.println("Meddelandet skickat! Statuskod: " + response.statusCode());
    }

    public Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> messageHandler) {
        AtomicBoolean isOpen = new AtomicBoolean(true);  //Spåra prenumerationens status

        http.sendAsync(
                HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(baseUrl + "/" + topic + "/json"))
                .build(),
                HttpResponse.BodyHandlers.ofLines()
        ).thenAccept(response -> {
            System.out.println("Prenumeration startad för topic: " + topic);  //Debug-loggning

            //Logga varje rad som mottas från servern
            response.body()
                    .peek(line -> System.out.println("Mottagen rad från servern: " + line))
                    .takeWhile(line -> isOpen.get())  //Avbryt strömmen om isOpen = false
                    .map(line-> {
                        try {
                            NtfyMessage message = mapper.readValue(line, NtfyMessage.class);
                            System.out.println("Parsat meddelande: " + message); //Debug-loggning
                            return message;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(message -> message.event().equals("message"))
                    .forEach(message -> {
                        if (isOpen.get()) {   //Endast om prenumerationen är öppen
                            Platform.runLater(() -> messageHandler.accept(message));
                        }
                    });
        }).exceptionally(error -> {
            System.err.println("Fel vid prenumeration: " + error.getMessage());
            return null;
        });

        //Command Pattern: Returnera ett Subscription-objekt
        return new Subscription() {
            @Override
            public void close() throws IOException {
                isOpen.set(false);  //Stäng strömmen
            }

            @Override
            public boolean isOpen() {
                return isOpen.get();
            }
        };
    }
}
