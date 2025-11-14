package com.example;

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
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String hostName;
    private final String topic;

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("NTFY_BASE_URL"), "Missing NTFY_BASE_URL in .env");
        topic = Objects.requireNonNull(dotenv.get("NTFY_TOPIC"), "Missing NTFY_TOPIC in .env");
    }

    @Override
    public boolean send(String message) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/" + topic))
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();
            var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> handler) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hostName + "/" + topic + "/json"))
                .GET()
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(resp -> resp.body().forEach(line -> {
                    try {
                        NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                        if ("message".equals(msg.event()) || "file".equals(msg.event()) || msg.hasAttachment()) {
                            handler.accept(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }))
                .exceptionally(t -> { t.printStackTrace(); return null; });
    }


    @Override
    public boolean sendFile(File file) {
        if (file == null || !file.exists()) return false;
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "application/octet-stream";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/" + topic))
                    .header("Content-Type", contentType)
                    .header("Filename", file.getName())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            var resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() >= 200 && resp.statusCode() < 300;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
