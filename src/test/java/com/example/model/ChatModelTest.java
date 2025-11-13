package com.example.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ChatModelTest {

    @Test
    @DisplayName("NtfyMessage should be created with topic and message")
    void testMessageCreation() {
        NtfyMessage message = new NtfyMessage("testTopic", "Hello");

        assertEquals("testTopic", message.topic());
        assertEquals("Hello", message.message());
        assertNotNull(message);
    }

    @Test
    @DisplayName("NtfyMessage should have event type")
    void testMessageEvent() {
        NtfyMessage message = new NtfyMessage("topic", "msg");

        assertEquals("message", message.event());
    }

    @Test
    @DisplayName("NtfyMessage with attachment should store filename")
    void testMessageWithAttachment() {
        NtfyMessage message = new NtfyMessage("topic", "File sent", "test.pdf");

        assertEquals("test.pdf", message.attachment());
        assertEquals("File sent", message.message());
    }
}