package com.example.network;

import com.example.model.NtfyMessage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public interface ChatNetworkClient {
    void send(String baseUrl, NtfyMessage message) throws IOException, InterruptedException;

    void sendFile(String baseUrl, String topic, File file) throws IOException, InterruptedException;

    Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> onMessage, Consumer<Throwable> onError);

    interface Subscription {
        void close();
    }
}