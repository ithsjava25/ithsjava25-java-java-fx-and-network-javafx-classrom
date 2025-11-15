package com.example.chat.model;

import com.example.chat.ChatModel;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatModelTest {

    private ChatModel model;

    @BeforeEach
    void setUp() {
        model = new ChatModel();
    }

    @Test
    void testAddMessage() {
        model.addMessage("Hello");
        ObservableList<String> messages = model.getMessages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).contains("Me: Hello"));
    }

    @Test
    void testAddEmptyMessage() {
        model.addMessage("");
        ObservableList<String> messages = model.getMessages();
        assertEquals(0, messages.size());
    }
}
