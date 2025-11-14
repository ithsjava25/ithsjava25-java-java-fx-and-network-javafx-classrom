package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile boolean running = true;
    private Thread receiverThread;

    public NtfyConnectionImpl(String hostName) {
        if (hostName == null || hostName.isBlank()) {
            throw new IllegalStateException("HOST_NAME is not configured for NtfyConnectionImpl");
        }
        this.hostName = hostName;
    }

    @Override
    public void send(String message) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/" + HelloModel.DEFAULT_TOPIC))
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Sent message, status: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendImage(java.io.File imageFile, String clientId) {
        throw new UnsupportedOperationException("sendImage is handled in HelloModel now.");
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        running = true;
        receiverThread = new Thread(() -> {
            while (running) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(hostName + "/" + HelloModel.DEFAULT_TOPIC + "/json"))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String body = response.body();
                    for (String line : body.split("\\R")) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        if (line.startsWith("data:")) line = line.substring(5).trim();

                        try {
                            NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                            if ("message".equals(msg.event())) {
                                messageHandler.accept(msg);
                            }
                        } catch (Exception ignored) {}
                    }

                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    if (!running) break;
                    try { Thread.sleep(2000); } catch (InterruptedException ex) { break; }
                }
            }
        }, "NtfyReceiverThread");
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    @Override
    public void stopReceiving() {
        running = false;
        if (receiverThread != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
    }
}
