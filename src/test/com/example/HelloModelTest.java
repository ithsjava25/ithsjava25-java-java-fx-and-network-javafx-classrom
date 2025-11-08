package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HelloModelTest {

    private HelloModel model;

    @BeforeEach
    void setUp() {
        model = new HelloModel();
    }

    @Test
    void testAddMessageStoresMessages() {
        model.addMessage("Hello");
        model.addMessage("World");

        List<String> messages = model.getMessages();

        assertEquals(2, messages.size(), "Should store exactly two messages");
        assertEquals("Hello", messages.get(0), "First message should match");
        assertEquals("World", messages.get(1), "Second message should match");
    }

    @Test
    void testGetMessagesReturnsListReference() {
        List<String> messages = model.getMessages();
        assertNotNull(messages, "getMessages() should never return null");
    }

    @Test
    void testMessagesListIsMutable() {

        List<String> messages = model.getMessages();
        messages.add("Direct modification");

        assertEquals(1, model.getMessages().size());
    }

    @Test
    void testEmptyInitially() {
        assertTrue(model.getMessages().isEmpty(), "New model should start with an empty message list");
    }
}
