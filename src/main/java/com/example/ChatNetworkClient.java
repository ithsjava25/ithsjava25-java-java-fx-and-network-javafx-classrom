package com.example;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

public interface ChatNetworkClient {
    void send(String baseUrl, NtfyMessage message) throws Exception;

    //Returnerar mitt egna Subscription-interface
    Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> messageHandler);

    //Inner interface för Subscription
    interface Subscription extends Closeable {
        @Override
        void close() throws IOException;
        boolean isOpen();
    }
}
