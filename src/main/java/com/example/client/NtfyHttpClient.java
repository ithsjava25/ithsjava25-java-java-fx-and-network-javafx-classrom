package com.example.client;

import com.example.domain.ChatModel;
import com.example.domain.NtfyEventResponse;
import com.example.domain.NtfyMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                .header("accept", "application/json")
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
                    NtfyEventResponse msg = mapper.readValue(line, NtfyEventResponse.class);
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
    public void send(String baseUrl, NtfyMessage msg, java.io.File attachment) throws IOException, InterruptedException {

        if (attachment != null) {
            sendWithAttachment(baseUrl, msg, attachment);
            return;
        }

        sendJsonOnly(baseUrl, msg);
    }

    private void sendJsonOnly(String baseUrl, NtfyMessage msg) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(msg);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClientProvider.get().send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Sent JSON payload: {}", json);
    }

    private void sendWithAttachment(String baseUrl, NtfyMessage msg, java.io.File file)
            throws IOException, InterruptedException {

        String topicUrl = baseUrl + "/" + msg.topic();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(topicUrl))
                .header("Filename", file.getName())
                .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                .build();


        var response = HttpClientProvider.get().send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Attachment sent: {} (status {})", file.getName(), response.statusCode());
    }


}
