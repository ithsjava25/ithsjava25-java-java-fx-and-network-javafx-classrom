package com.example;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.function.Consumer;

public class HelloModel {

    private final String TOPIC_URL;

    public HelloModel() {
        TOPIC_URL = EnvLoader.get("NTFY_URL");
        if (TOPIC_URL == null || TOPIC_URL.isBlank()) {
            throw new IllegalStateException("NTFY_URL not found in .env file");
        }
    }

    public void sendMessage(String username, String message) throws Exception {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("message", message);
        json.put("time", Instant.now().getEpochSecond());

        byte[] out = json.toString().getBytes();

        URL url = new URL(TOPIC_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.getOutputStream().write(out);

        int response = conn.getResponseCode();
        System.out.println("POST response: " + response);

        conn.getInputStream().close();
        conn.disconnect();
    }


    public void listenForMessages(Consumer<ChatMessage> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(TOPIC_URL + "/json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;

                    JSONObject envelope = new JSONObject(line);

                    if (!envelope.has("message")) continue;

                    String rawMsg = envelope.getString("message");
                    JSONObject json;

                    try {
                        json = new JSONObject(rawMsg);
                    } catch (Exception e) {
                        json = new JSONObject();
                        json.put("username", "unknown");
                        json.put("message", rawMsg);
                        json.put("time", envelope.optLong("time", Instant.now().getEpochSecond()));
                    }

                    String username = json.optString("username", "unknown");
                    String message  = json.optString("message", "");
                    String fileName = json.optString("file_name", null);

                    long rawTime = json.optLong("time", envelope.optLong("time", Instant.now().getEpochSecond()));
                    String timestamp = Instant.ofEpochSecond(rawTime)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    String fileData = json.optString("file_data", null);
                    ChatMessage msg;
                    if (fileName != null) {
                        msg = new ChatMessage(username, message, timestamp, fileName, fileData);
                    } else {
                        msg = new ChatMessage(username, message, timestamp);
                    }

                    callback.accept(msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendFile(String username, File file) {
        if (file == null || !file.exists()) return;
        final String safeUsername = (username == null || username.isBlank()) ? "unknown" : username;

        new Thread(() -> {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String encoded = Base64.getEncoder().encodeToString(bytes);

                JSONObject json = new JSONObject();
                json.put("username", safeUsername);
                json.put("message", "Sent file: " + file.getName());
                json.put("time", Instant.now().getEpochSecond());
                json.put("file_name", file.getName());
                json.put("file_data", encoded);

                sendJsonToNtfy(json);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendJsonToNtfy(JSONObject json) throws IOException {
        byte[] out = json.toString().getBytes();

        URL url = new URL(TOPIC_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setFixedLengthStreamingMode(out.length); // nice to set Content-Length
        conn.connect();

        try (OutputStream os = conn.getOutputStream()) {
            os.write(out);
            os.flush();
        }

        int rc = conn.getResponseCode();
        System.out.println("sendJsonToNtfy() -> response: " + rc);

        if (rc < 200 || rc >= 300) {
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    String body = new String(err.readAllBytes());
                    System.err.println("ntfy error body: " + body);
                }
            }
        }

        conn.getInputStream().close();
        conn.disconnect();
    }
    public void loadHistory(Consumer<ChatMessage> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(TOPIC_URL + "/json?since=all");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;

                    JSONObject envelope = new JSONObject(line);
                    if (!envelope.has("message")) continue;

                    JSONObject json;
                    try {
                        json = new JSONObject(envelope.getString("message"));
                    } catch (Exception e) {
                        json = new JSONObject();
                        json.put("username", "unknown");
                        json.put("message", envelope.getString("message"));
                    }

                    String username = json.optString("username", "unknown");
                    String message = json.optString("message", "");
                    String fileName = json.optString("file_name", null);

                    long time = json.optLong("time", envelope.optLong("time", Instant.now().getEpochSecond()));
                    String timestamp = Instant.ofEpochSecond(time)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    String fileData = json.optString("file_data", null);

                    ChatMessage msg = (fileName != null)
                            ? new ChatMessage(username, message, timestamp, fileName, fileData)
                            : new ChatMessage(username, message, timestamp);

                    callback.accept(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


}
