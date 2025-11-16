package com.example.domain;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class ChatModel {
    private final ObservableList<NtfyMessage> messages = FXCollections.observableArrayList();


    public void addMessage(NtfyMessage msg) {
        runOnFx(() -> messages.add(msg));
    }

    public ObservableList<NtfyMessage> getMessages() {
        return messages;
    }

    private static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }

}
