package com.example.client;

import com.example.domain.ChatModel;
import com.example.domain.NtfyMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class NtfyHttpClient implements ChatNetworkClient {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(NtfyHttpClient.class);
    private final ChatModel model;

    public NtfyHttpClient(ChatModel model) {
        this.model = model;
    }

    @Override
    public Subscription subscribe(String baseUrl, String topic) {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + topic + "/json"))
                .GET()
                .build();

        CompletableFuture<HttpResponse<java.util.stream.Stream<String>>> future =
                HttpClientProvider.get().sendAsync(req, HttpResponse.BodyHandlers.ofLines());

        AtomicBoolean open = new AtomicBoolean(true);

        future.thenAccept(response -> {
            response.body().forEach(line -> {
                if (!open.get()) return;

                try {
                    NtfyMessage msg = mapper.readValue(line, NtfyMessage.class);
                    if (msg.event().equals("message")) {
                        Platform.runLater(() ->
                            model.addMessage(msg)
                        );
                    }
                } catch (Exception _) {
                }
            });
        });
        log.info("Successfully subscribed to topic: {}", topic);

        return new Subscription() {
            @Override
            public void close() throws IOException {
                open.set(false);
                future.cancel(true);
            }

            @Override
            public boolean isOpen() {
                return open.get();
            }
        };
    }

    @Override
    public void send(String baseUrl, NtfyMessage msg) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + msg.topic()))
                .POST(HttpRequest.BodyPublishers.ofString(msg.message()))
                .build();

        try {
            HttpClientProvider.get()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            log.info("ok");

        } catch (HttpTimeoutException e) {
            log.error("Timeout while sending message", e);

        } catch (IOException e) {
            log.error("IO error while sending message", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Send operation interrupted", e);
        }
    }
}
