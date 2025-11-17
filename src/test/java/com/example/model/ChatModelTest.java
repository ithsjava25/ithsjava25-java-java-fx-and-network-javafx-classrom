package com.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ChatModelTest {

    private ChatModel model;

    @BeforeEach
    void setup() {
        model = new ChatModel();
    }

    @Test
    @DisplayName("Should have empty message list initially")
    void testInitialState() {
        assertNotNull(model.getMessages());
        assertTrue(model.getMessages().isEmpty());
    }

    @Test
    @DisplayName("Should return observable list of messages")
    void testGetMessages() {
        assertNotNull(model.getMessages());
        assertEquals(0, model.getMessages().size());
    }

    @Test
    @DisplayName("Should allow adding messages to list")
    void testAddMessageToList() {
        NtfyMessage message = new NtfyMessage("testTopic", "Test message");
        model.getMessages().add(message);

        assertEquals(1, model.getMessages().size());
        assertEquals("Test message", model.getMessages().get(0).message());
    }

    @Test
    @DisplayName("Should maintain message order")
    void testMessageOrder() {
        NtfyMessage msg1 = new NtfyMessage("topic", "First");
        NtfyMessage msg2 = new NtfyMessage("topic", "Second");
        NtfyMessage msg3 = new NtfyMessage("topic", "Third");

        model.getMessages().add(msg1);
        model.getMessages().add(msg2);
        model.getMessages().add(msg3);

        assertEquals("First", model.getMessages().get(0).message());
        assertEquals("Second", model.getMessages().get(1).message());
        assertEquals("Third", model.getMessages().get(2).message());
    }
}