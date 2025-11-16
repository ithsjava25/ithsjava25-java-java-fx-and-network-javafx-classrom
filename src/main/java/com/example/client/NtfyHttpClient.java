package com.example.client;

import com.example.domain.ChatModel;
import com.example.domain.NtfyMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class NtfyHttpClient implements ChatNetworkClient {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger("NtfyClient");
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
                log.info("Event received: {}", line);
                if (!open.get()) return;

                try {
                    NtfyMessage msg = mapper.readValue(line, NtfyMessage.class);
                    if (msg.event().equals("message")) {
                        model.addMessage(msg);
                        log.info("Message added: {}", msg);
                    }
                } catch (JsonProcessingException e)  {
                    throw new RuntimeException(e);
                }
            });
        });
        log.info("Successfully subscribed to topic: {}", topic);

        return new Subscription() {
            @Override
            public void close() {
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
    public void send(String baseUrl, NtfyMessage msg) throws IOException, InterruptedException {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + msg.topic()))
                .POST(HttpRequest.BodyPublishers.ofString(msg.message()));

        if (msg.title() != null) {
            builder.header("title", msg.title());
        }

        if (msg.tags() != null && !msg.tags().isEmpty()) {
            String tagsHeader = String.join(",", msg.tags());
            builder.header("tags", tagsHeader);
        }

        HttpRequest request = builder.build();
        HttpClientProvider.get().send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Successfully sent message: {}", msg.message());
    }

}
