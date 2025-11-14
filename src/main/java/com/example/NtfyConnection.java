package com.example;

import java.util.function.Consumer;

interface NtfyConnection {
        void send(String message, Consumer<Boolean> callback);
        void receive(Consumer<NtfyMessageDto> handler);
    }

    //public boolean send(String message);

    //public void receive(Consumer<NtfyMessageDto> messageHandler);
