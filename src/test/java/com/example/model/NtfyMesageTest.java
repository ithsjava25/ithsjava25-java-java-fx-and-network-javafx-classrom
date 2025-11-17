package com.example.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class NtfyMessageTest {

    @Test
    @DisplayName("Should create message with topic and message text")
    void testCreateMessage() {
        NtfyMessage message = new NtfyMessage("testTopic", "Hello World");

        assertEquals("testTopic", message.topic());
        assertEquals("Hello World", message.message());
        assertNotNull(message);
    }

    @Test
    @DisplayName("Should set default event type to 'message'")
    void testDefaultEventType() {
        NtfyMessage message = new NtfyMessage("testTopic", "Test");

        assertEquals("message", message.event());
    }

    @Test
    @DisplayName("Should handle null id and time for new messages")
    void testNewMessageHasNullIdAndTime() {
        NtfyMessage message = new NtfyMessage("testTopic", "Test");

        assertNull(message.id());
        assertNull(message.time());
    }

    @Test
    @DisplayName("Should create message with all fields using canonical constructor")
    void testFullConstructor() {
        NtfyMessage message = new NtfyMessage(
                "abc123",       // id
                1234567890L,    // time
                "message",      // event
                "testTopic",    // topic
                "Hello",        // message
                null            // attachment
        );

        assertEquals("abc123", message.id());
        assertEquals(1234567890L, message.time());
        assertEquals("message", message.event());
        assertEquals("testTopic", message.topic());
        assertEquals("Hello", message.message());
    }

    @Test
    @DisplayName("Should accept empty message text")
    void testEmptyMessage() {
        NtfyMessage message = new NtfyMessage("testTopic", "");

        assertEquals("", message.message());
        assertEquals("testTopic", message.topic());
    }

    @Test
    @DisplayName("Should create message with attachment")
    void testMessageWithAttachment() {
        NtfyMessage message = new NtfyMessage("testTopic", "File sent", "test.pdf");

        assertEquals("testTopic", message.topic());
        assertEquals("File sent", message.message());
        assertEquals("test.pdf", message.attachment());
    }

    @Test
    @DisplayName("Should handle special characters in message")
    void testSpecialCharacters() {
        String specialMessage = "Hello! 🌸 @user #topic";
        NtfyMessage message = new NtfyMessage("testTopic", specialMessage);

        assertEquals(specialMessage, message.message());
    }
}