package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;

import java.io.*;
import java.net.*;
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
    public void send(String message) {
        System.out.println("Sending to: " + hostName + "/MartinsTopic");
        System.out.println("Message content: " + message);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/MartinsTopic"))
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }


    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        Thread thread = new Thread(() -> {
            while (running) { // 🔄 använd flagga istället för while(true)
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(hostName + "/MartinsTopic/json"))
                        .build();

                try {
                    client.send(request, HttpResponse.BodyHandlers.ofLines())
                            .body()
                            .forEach(line -> {
                                try {
                                    NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);
                                    if ("message".equals(msg.event())) {
                                        Platform.runLater(() -> messageHandler.accept(msg));
                                    }
                                } catch (JsonProcessingException e) {
                                    // Ignorera ogiltiga rader
                                }
                            });
                } catch (IOException | InterruptedException e) {
                    if (!running) break; // 🛑 avsluta om stop() kallats
                    System.out.println("Disconnected from ntfy, reconnecting...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
            System.out.println("🔴 NtfyReceiverThread stopped.");
        }, "NtfyReceiverThread");

        thread.setDaemon(true);
        thread.start();
        this.receiverThread = thread;
    }

    private volatile boolean running = true;
    private Thread receiverThread;

    /** Stoppar mottagartråden på ett säkert sätt */
    public void stopReceiving() {
        running = false;
        if (receiverThread != null && receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
    }


    // 🟢 Ny metod för att skicka bilder
    @Override
    public boolean sendImage(File imageFile, String clientId) {
        try {
            String boundary = "----JavaFXChatBoundary";
            URL url = new URL(hostName + "/MartinsTopic");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setRequestProperty("X-Client-Id", clientId);


            try (OutputStream output = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

                // 🟢 1️⃣ Lägg till ett textfält med clientId i själva meddelandet
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
                writer.append("{\"clientId\":\"").append(clientId)
                        .append("\",\"type\":\"image\",\"fileName\":\"")
                        .append(imageFile.getName()).append("\"}\r\n");
                writer.flush();

                // 🟢 2️⃣ Bilddelen
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                        .append(imageFile.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(Files.probeContentType(imageFile.toPath())).append("\r\n\r\n");
                writer.flush();

                Files.copy(imageFile.toPath(), output);
                output.flush();

                writer.append("\r\n--").append(boundary).append("--\r\n").flush();
            }

            int responseCode = conn.getResponseCode();
            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("📸 Image upload response: " + responseCode);
            System.out.println("📝 Response body: " + responseBody);

            conn.disconnect();
            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }







}
