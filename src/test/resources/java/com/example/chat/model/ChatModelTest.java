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
    void testAddMessageAddsToList() {
        ObservableList<String> messages = model.getMessages();
        assertEquals(0, messages.size());

        model.addMessage("Hello");
        assertEquals(1, messages.size());
        assertEquals("Me: Hello", messages.get(0));
    }

    @Test
    void testAddMessageIgnoresEmpty() {
        ObservableList<String> messages = model.getMessages();
        model.addMessage("");
        model.addMessage("   ");
        assertEquals(0, messages.size());
    }
}

