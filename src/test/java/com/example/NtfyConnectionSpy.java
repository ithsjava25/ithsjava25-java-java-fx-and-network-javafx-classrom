package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    public String messageSent;
    public String topicSent = null;
    String message;
    public Consumer<NtfyMessageDto> messageHandler;
    public File fileSent;
    public String fileTopicSent = null;
    private String currentTopic = "general";


    @Override
    public String getTopic() {
        return currentTopic;
    }

    @Override
    public boolean send(String message, String topic) {
        this.message = message;
        this.topicSent = topic;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler=messageHandler;

    }

    @Override
    public boolean sendFile(File file, String topic) {
        this.fileSent=file;
        this.fileTopicSent = topic;
        return true;
    }

    @Override
    public void connect(String topic, Consumer<NtfyMessageDto> messageHandler) {
        this.currentTopic = topic;
        this.messageHandler = messageHandler;
    }
}
