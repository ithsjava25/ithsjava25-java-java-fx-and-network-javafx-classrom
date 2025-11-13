package com.example.network;

import com.example.model.NtfyMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyHttpClient implements ChatNetworkClient {

    private final HttpClient http;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new NtfyHttpClient configured for HTTP requests and JSON (de)serialization.
     *
     * Initializes an internal HttpClient for network operations and a Jackson ObjectMapper
     * for JSON serialization and deserialization.
     */
    public NtfyHttpClient() {
        this.http = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends the given NtfyMessage to the specified base URL using an HTTP POST.
     *
     * The message is serialized to JSON and posted to the base URL (any trailing slash is removed).
     *
     * @param baseUrl the destination base URL for the POST request
     * @param message the message to serialize and send
     * @throws IOException if the request fails or the response status code is not 200
     * @throws InterruptedException if the thread is interrupted while waiting for the response
     */
    @Override
    public void send(String baseUrl, NtfyMessage message) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ensureNoTrailingSlash(baseUrl)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to send message. Status: " + response.statusCode());
        }
    }

    /**
     * Uploads the given file to the Ntfy server by sending its bytes with an HTTP PUT to {@code baseUrl}/{@code topic}.
     *
     * @param baseUrl the base URL of the Ntfy server (may include or omit a trailing slash)
     * @param topic   the topic path segment to upload the file to
     * @param file    the file to upload
     * @throws IOException if the file does not exist or the server responds with a non-200 status (response body included in the message)
     * @throws InterruptedException if the HTTP request is interrupted
     */
    @Override
    public void sendFile(String baseUrl, String topic, File file) throws IOException, InterruptedException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getName());
        }

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String url = ensureNoTrailingSlash(baseUrl) + "/" + topic;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Filename", file.getName())
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            String errorBody = response.body();
            throw new IOException("Failed to send file. Status: " + response.statusCode() + ", Error: " + errorBody);
        }

        System.out.println("✅ File uploaded successfully: " + file.getName());
    }

    /**
     * Subscribes to a topic's JSON event stream and dispatches incoming "message" events to a consumer.
     *
     * @param baseUrl the base URL of the ntfy server (may include scheme and host)
     * @param topic the topic to subscribe to
     * @param onMessage consumer invoked for each received `NtfyMessage` whose `event` equals `"message"`
     * @param onError consumer invoked for any error encountered while receiving or parsing events
     * @return a Subscription that cancels the underlying request and stops delivery when invoked
     */
    @Override
    public Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> onMessage, Consumer<Throwable> onError) {
        String url = ensureNoTrailingSlash(baseUrl) + "/" + topic + "/json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        CompletableFuture<Void> future = http.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.trim().isEmpty()) {
                                NtfyMessage message = objectMapper.readValue(line.trim(), NtfyMessage.class);
                                if ("message".equals(message.event())) {
                                    onMessage.accept(message);
                                }
                            }
                        }
                    } catch (Exception e) {
                        onError.accept(e);
                    }
                })
                .exceptionally(error -> {
                    onError.accept(error);
                    return null;
                });

        return () -> future.cancel(true);
    }

    /**
     * Normalize a URL by removing a trailing slash if present.
     *
     * @param url the URL string to normalize; may end with a slash
     * @return the URL without a trailing slash if one was present, otherwise the original URL
     */
    private String ensureNoTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}