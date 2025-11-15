package com.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String hostName;
    private final String topic;

    /**
     * Default constructor that loads configuration from .env file
     * Initializes HTTP client and JSON mapper with required settings
     */
    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();

        this.hostName = Objects.requireNonNull(dotenv.get("NTFY_BASE_URL"), "Missing NTFY_BASE_URL in .env");
        this.topic = Objects.requireNonNull(dotenv.get("NTFY_TOPIC"), "Missing NTFY_TOPIC in .env");

        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Checks if an HTTP response indicates success
     * @param resp the HTTP response to check
     * @return true if status code is in 200-299 range, false otherwise
     */
    private boolean isSuccess(HttpResponse<?> resp) {
        int c = resp.statusCode();
        return c >= 200 && c < 300;
    }

    @Override
    /**
     * Sends a text message to the NTFY topic
     * @param message the text message to send to the NTFY topic
     * @return true if the message was sent successfully, false if an error occurred
     */
    public boolean send(String message) {

        System.out.println("📤 RAW SENT: " + message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hostName + "/" + topic))
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        try {
            http.send(request, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (Exception e) {
            System.err.println("❌ SEND ERROR: " + e.getMessage());
            return false;
        }
    }

    @Override
    /**
     * Starts receiving messages from the NTFY topic asynchronously
     * @param handler the consumer callback that processes incoming NTFY messages
     */
    public void receive(Consumer<NtfyMessageDto> handler) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(hostName + "/" + topic + "/json"))
                .GET()
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(resp -> {
                    if (!isSuccess(resp)) {
                        System.err.println("❌ SUBSCRIBE FAILED: " + resp.statusCode());
                        return;
                    }

                    resp.body().forEach(line -> {
                        if (line.isBlank()) return;

                        System.out.println("📥 RAW RECEIVED: " + line);

                        try {
                            NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                            handler.accept(msg);
                        } catch (Exception e) {
                            System.err.println("❌ JSON ERROR: " + e.getMessage());
                        }
                    });
                })
                .exceptionally(err -> {
                    System.err.println("❌ RECEIVE ERROR: " + err.getMessage());
                    return null;
                });
    }


    @Override
    /**
     * Sends a file attachment to the NTFY topic
     * @param file the file to send as an attachment to the NTFY topic
     * @return true if the file was sent successfully, false if the file is missing or an error occurred
     */
    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            System.err.println("❌ File missing");
            return false;
        }

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            String type = Files.probeContentType(file.toPath());
            if (type == null) type = "application/octet-stream";

            System.out.println("📤 RAW SENT (FILE): " + file.getName() +
                    " (" + data.length + " bytes)");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/" + topic))
                    .header("Content-Type", type)
                    .header("Filename", file.getName())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());
            return true;

        } catch (Exception e) {
            System.err.println("❌ FILE SEND ERROR: " + e.getMessage());
            return false;
        }
    }
}
