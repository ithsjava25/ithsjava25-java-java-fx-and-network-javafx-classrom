package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient client = HttpClient.newHttpClient();
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
    public void send(String jsonMessage) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/" + HelloModel.DEFAULT_TOPIC))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonMessage))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Sent message, status: " + response.statusCode());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendImage(File imageFile, String clientId) {
        try {
            String boundary = "----JavaFXChatBoundary";
            URL url = new URL(hostName + "/" + HelloModel.DEFAULT_TOPIC);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("X-Client-Id", clientId);

            var json = mapper.writeValueAsString(
                    new java.util.LinkedHashMap<String,Object>() {{
                        put("clientId", clientId);
                        put("type", "image");
                        put("fileName", imageFile.getName());
                    }}
            );

            try (OutputStream output = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
                writer.append(json).append("\r\n");
                writer.flush();

                String contentType = Files.probeContentType(imageFile.toPath());
                if (contentType == null) contentType = "application/octet-stream";

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(imageFile.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(contentType).append("\r\n\r\n");
                writer.flush();

                Files.copy(imageFile.toPath(), output);
                output.flush();

                writer.append("\r\n--").append(boundary).append("--\r\n").flush();
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();
            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        Thread thread = new Thread(() -> {
            while (running) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(hostName + "/MartinsTopic/json"))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String body = response.body();

                    for (String line : body.split("\\R")) {
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        if (line.startsWith("data:")) {
                            line = line.substring(5).trim();
                        }

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

        thread.setDaemon(true);
        thread.start();
        this.receiverThread = thread;
    }


    private volatile boolean running = true;
    private Thread receiverThread;

    public void stopReceiving() {
        running = false;
        if (receiverThread != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
    }
}
