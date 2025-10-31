package com.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */

    private final String topic = "javafx-demo";
    private final String baseUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface MessageListener {
        void onMessage(String message);
    }

    public HelloModel() {
    this.baseUrl = System.getenv().getOrDefault("NTFY_URL", "https://ntfy.sh");
    }

    public void sendMessage(String message) throws IOException {
        URL url = new URL (baseUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String json = String.format("{\"topic\":\"%s\",\"message\":\"%s\"}",topic, message);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        conn.getResponseCode();
        conn.disconnect();
    }
    public void startListening (MessageListener listener) {
        executor.submit(()->{
            try {
                URL url = new URL (baseUrl + "/" + topic + "/json");
                BufferedReader rader = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                while ((line = rader.readLine()) != null) {
                    if (line.contains("\"nessage\"")) {
                    String msg = line.replaceAll(".*\"message\":\"(.*?)\".*", "$1");
                    listener.onMessage(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
