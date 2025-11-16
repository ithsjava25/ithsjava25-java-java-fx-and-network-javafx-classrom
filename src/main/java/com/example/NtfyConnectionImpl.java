package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
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
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .header("Content-Type", "text/plain; charset=utf-8")
                .uri(URI.create(hostName + "/mytopic"))
                .build();

        try {
            http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message");
        } catch (InterruptedException e) {
            System.out.println("Interrupted sending message");
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public boolean sendFile(Path file) throws FileNotFoundException {
        String filename = file.getFileName().toString();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofFile(file))
                .header("Filename", filename)
                .uri(URI.create(hostName + "/mytopic"))
                .build();

        try {
            http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return true;
        } catch (IOException e) {
            System.out.println("Error sending file");
        } catch (InterruptedException e) {
            System.out.println("Interrupted sending file");
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response ->
                        response.body()
                        .map(this::tryParse)
                        .filter(m -> "message".equals(m.event()))
                        .forEach(messageHandler));
    }

    @Override
    public List<NtfyMessageDto> fetchHistory() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json?poll=1&since=all"))
                .build();
        List<NtfyMessageDto> list = new ArrayList<>();
        try {
            HttpResponse<java.util.stream.Stream<String>> response =
                    http.send(request, HttpResponse.BodyHandlers.ofLines());
            response.body()
                    .map(this::tryParse)
                    .filter(Objects::nonNull)
                    .filter(m -> "message".equals(m.event()))
                    .forEach(list::add);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
        return list;
    }

    private NtfyMessageDto tryParse(String line) {
        try {
            return mapper.readValue(line, NtfyMessageDto.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
