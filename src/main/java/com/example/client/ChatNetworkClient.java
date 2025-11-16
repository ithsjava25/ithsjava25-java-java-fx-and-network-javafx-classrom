package com.example.client;


public interface ChatNetworkClient {
    Subscription subscribe(String baseUrl, String topic);
    void send(String baseUrl, String topic, String message);

    interface Subscription extends AutoCloseable {
        @Override
        void close() throws Exception;
        boolean isOpen();
    }
}
