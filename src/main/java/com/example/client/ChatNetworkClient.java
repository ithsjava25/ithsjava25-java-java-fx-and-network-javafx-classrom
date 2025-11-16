package com.example.client;


import com.example.domain.NtfyMessage;

import java.io.IOException;

public interface ChatNetworkClient {
    Subscription subscribe(String baseUrl, String topic);
    void send(String baseUrl, NtfyMessage message);

    interface Subscription extends AutoCloseable {
        @Override
        void close() throws IOException;
        boolean isOpen();
    }
}
