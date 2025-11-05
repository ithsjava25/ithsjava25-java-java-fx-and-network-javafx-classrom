package com.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class NtfyClient {

    private final String topic;
    private final HttpClient client;

    public NtfyClient(String topic) {
        this.topic = topic;
        this.client = HttpClient.newHttpClient();
    }

    // ---- SEND ----
    public void sendMessage(String username, String message) {
        try {
            String json = String.format("""
            {
              "topic": "%s",
              "message": "%s",
              "title": "%s"
            }
            """, topic, message.replace("\"", "\\\""), username);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- RECEIVE ----
    public void subscribe(MessageHandler handler) {
        new Thread(() -> {
            while (true) { // reconnect loop for robustness
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://ntfy.sh/" + topic + "/json"))
                            .GET()
                            .build();

                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.isBlank()) {
                                JSONObject obj = new JSONObject(line);
                                if ("message".equals(obj.optString("event"))) {
                                    String user = obj.optString("title", "Anonymous");
                                    String msg = obj.optString("message", "");
                                    handler.onMessage(user + ": " + msg);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Stream disconnected, retrying...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }, "Ntfy-Listener").start();
    }

    public interface MessageHandler {
        void onMessage(String message);
    }
}
