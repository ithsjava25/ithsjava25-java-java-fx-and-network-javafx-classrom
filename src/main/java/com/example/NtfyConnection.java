package com.example;

import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {

    SimpleStringProperty topic = new SimpleStringProperty();

    public SimpleStringProperty topicProperty();

    public String getTopic();

    public void setTopic(String topic);

    public boolean send(String message);

    public void recieve(Consumer<NtfyMessage> messageHandler);

    public boolean sendFile(File attachment);
}
