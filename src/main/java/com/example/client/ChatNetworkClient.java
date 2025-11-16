package com.example.client;


import java.io.IOException;

public interface ChatNetworkClient {
    Subscription subscribe(String baseUrl, String topic);
    void send(String baseUrl, String topic, String message);

    interface Subscription extends AutoCloseable {
        @Override
        void close() throws IOException;
        boolean isOpen();
    }
}
