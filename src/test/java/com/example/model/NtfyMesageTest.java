package com.example.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class NtfyMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should create message with convenience constructor")
    void testConvenienceConstructor() {
        NtfyMessage message = new NtfyMessage("testTopic", "Hello World");

        assertEquals("testTopic", message.topic());
        assertEquals("Hello World", message.message());
        assertEquals("message", message.event());
        assertNull(message.id());
        assertNull(message.time());
    }

    @Test
    @DisplayName("Should deserialize JSON to NtfyMessage")
    void testDeserialization() throws Exception {
        String json = """
            {
                "id": "abc123",
                "time": 1234567890,
                "event": "message",
                "topic": "testTopic",
                "message": "Hello World"
            }
            """;

        NtfyMessage message = objectMapper.readValue(json, NtfyMessage.class);

        assertNotNull(message);
        assertEquals("abc123", message.id());
        assertEquals(1234567890, message.time());
        assertEquals("message", message.event());
        assertEquals("testTopic", message.topic());
        assertEquals("Hello World", message.message());
    }

    @Test
    @DisplayName("Should serialize NtfyMessage to JSON")
    void testSerialization() throws Exception {
        NtfyMessage message = new NtfyMessage("testTopic", "Hello World");

        String json = objectMapper.writeValueAsString(message);

        assertNotNull(json);
        assertTrue(json.contains("\"topic\":\"testTopic\""));
        assertTrue(json.contains("\"message\":\"Hello World\""));
        assertTrue(json.contains("\"event\":\"message\""));
    }

    @Test
    @DisplayName("Should ignore unknown fields during deserialization")
    void testIgnoreUnknownFields() throws Exception {
        String json = """
            {
                "id": "xyz789",
                "time": 9876543210,
                "event": "message",
                "topic": "testTopic",
                "message": "Test",
                "expires": 1761905659,
                "unknownField": "should be ignored"
            }
            """;

        NtfyMessage message = objectMapper.readValue(json, NtfyMessage.class);

        assertNotNull(message);
        assertEquals("xyz789", message.id());
        assertEquals("Test", message.message());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        NtfyMessage message = new NtfyMessage("topic", "message");

        assertNull(message.id());
        assertNull(message.time());
        assertNull(message.attachment());
    }
}