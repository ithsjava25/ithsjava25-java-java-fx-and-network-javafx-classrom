package com.example;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatModel {

    private final String sendUrl;
    private final String subscribeUrl;
    private final String clientId;
    private final Set<String> sentMessages = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final HttpClient httpClient;
    private final Consumer<Runnable> platformRunner;

    public ChatModel() {
        this(HttpClient.newHttpClient(), Platform::runLater);
    }

    public ChatModel(HttpClient httpClient, Consumer<Runnable> platformRunner) {
        String topic = System.getenv().getOrDefault("NTFY_TOPIC", "https://ntfy.sh/newchatroom3");
        this.sendUrl = topic;
        this.subscribeUrl = topic + "/sse";
        this.clientId = UUID.randomUUID().toString();
        this.httpClient = httpClient;
        this.platformRunner = platformRunner;
    }

    public void sendMessage(String message) {
        sentMessages.add(message);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            sentMessages.remove(message);
        }, "SentMessageCleaner").start();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sendUrl))
                .header("Content-Type", "application/json")
                .header("X-Client-ID", clientId)
                .header("Title", "Friend: ")
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> System.out.println("Sent, status=" + response.statusCode()))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


    public void subscribe(Consumer<String> onMessageReceived) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(subscribeUrl))
                .header("Accept", "text/event-stream")
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                        String line;
//                        boolean firstMessageSkipped = false;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data:")) {
//                                if (!firstMessageSkipped) {
//                                    firstMessageSkipped = true;
//                                    continue;
//                                }

                                String raw = line.substring(5).trim();
                                String msg = parseMessage(raw);

                                if (msg != null && !sentMessages.contains(msg)) {
                                    platformRunner.accept(() -> onMessageReceived.accept(msg));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .exceptionally(e -> { e.printStackTrace(); return null; });
    }

    public String parseMessage(String data) {
        try {
            Matcher eventMatcher = Pattern.compile("\"event\"\\s*:\\s*\"(.*?)\"").matcher(data);
            if (eventMatcher.find()) {
                String event = eventMatcher.group(1);
                if (!"message".equals(event)) {
                    return null;
                }
            }

            Matcher msgMatcher = Pattern.compile("\"message\"\\s*:\\s*\"(.*?)\"").matcher(data);
            if (msgMatcher.find()) {
                return msgMatcher.group(1).replace("\\\"", "\"");
            }
        } catch (Exception ignored) {}
        return null;
    }

}
