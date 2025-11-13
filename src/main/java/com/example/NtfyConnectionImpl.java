package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        String hostFromEnv = dotenv.get("HOST_NAME");

        if (hostFromEnv == null || hostFromEnv.isBlank()) {
            throw new IllegalStateException("HOST_NAME saknas i .env-filen!");
        }

        hostName = hostFromEnv;
        System.out.println("HOST_NAME: " + hostName);
    }

    @Override
    public boolean send(String message) {
        if (message == null || message.isBlank()) {
            System.err.println("⚠️ Meddelandet är tomt, inget skickas.");
            return false;
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .header("Cache", "no")
                .build();

        CompletableFuture<HttpResponse<Void>> future = http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding());

        future.exceptionally(ex -> {
            System.err.println("❌ Fel vid sändning: " + ex.getMessage());
            return null;
        });

        return true;
    }



    @Override
    public boolean sendFile(File file, String filename) {
        try {
            if (!file.exists()) {
                System.err.println("❌ Filen finns inte: " + file.getAbsolutePath());
                return false;
            }

            if (file.length() > 15 * 1024 * 1024) { // 15 MB limit
                System.err.println("❌ Filen är för stor: " + file.length() + " bytes");
                return false;
            }

            String actualFilename = (filename != null) ? filename : file.getName();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Filename", actualFilename)
                    .header("Cache", "no")
                    .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Fil skickad: " + actualFilename);
                return true;
            } else {
                System.err.println("❌ Fel vid filöverföring: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Fel vid filöverföring: " + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean sendFileFromUrl(String fileUrl, String filename) {
        try {
            String actualFilename = (filename != null) ? filename : extractFilenameFromUrl(fileUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Attach", fileUrl)
                    .header("Filename", actualFilename)
                    .header("Cache", "no")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Fil från URL skickad: " + actualFilename);
                return true;
            } else {
                System.err.println("❌ Fel vid URL-filöverföring: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Fel vid URL-filöverföring: " + e.getMessage());
            return false;
        }
    }

    private String extractFilenameFromUrl(String url) {
        try {
            return url.substring(url.lastIndexOf('/') + 1);
        } catch (Exception e) {
            return "file";
        }
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body().forEach(line -> {
                    try {
                        NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                        if ("message".equals(msg.event())) {
                            messageHandler.accept(msg);
                        }
                    } catch (JsonProcessingException e) {
                        System.err.println("⚠️ JSON parsing error: " + e.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    System.err.println("❌ Network error while receiving messages: " + ex.getMessage());
                    return null;
                });
    }
}
