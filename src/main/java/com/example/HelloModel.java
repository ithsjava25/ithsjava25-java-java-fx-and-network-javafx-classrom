package com.example;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class HelloModel {
    private final String baseUrl = "https://ntfy.sh";
    private final String topic = "java25-erika-chat";

    // Skickar ett textmeddelande till ntfy
    public void sendMessage(String message) {
        try {
            var url = new URL(baseUrl + "/" + topic);
            var conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.getOutputStream().write(message.getBytes());

            System.out.println("Skickat till " + url + ": " + message);
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Något gick fel: " + e.getMessage());
        }
    }

    // Startar en tråd som lyssnar efter nya meddelanden
    public void startListening(Consumer<String> onMessage, Consumer<Throwable> onError) {
        new Thread(() -> {
            try {
                var url = new URL(baseUrl + "/" + topic + "/json");
                var conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    // Plocka ut texten mellan "message":" och nästa "
                    int start = line.indexOf("\"message\":\"");
                    if (start != -1) {
                        start += 11;
                        int end = line.indexOf("\"", start);
                        if (end != -1) {
                            String msg = line.substring(start, end);
                            if (onMessage != null) onMessage.accept(msg);
                        }
                    }
                }

            } catch (Throwable ex) {
                if (onError != null) onError.accept(ex);
            }
        }).start();
    }

    // Visar vilken topic och url som används
    public String info() {
        return baseUrl + "/" + topic;
    }
}