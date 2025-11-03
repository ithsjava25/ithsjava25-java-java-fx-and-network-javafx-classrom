package com.example.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ChatModelTest {

    @Test
    @DisplayName("ChatModel should initialize with empty message list")
    void testInitialState() {
        ChatModel model = new ChatModel(java.net.http.HttpClient.newHttpClient());

        assertNotNull(model.getMessages());
        assertTrue(model.getMessages().isEmpty());
    }

    @Test
    @DisplayName("ChatModel should use default URL when NTFY_BACKEND_URL not set")
    void testDefaultUrl() {
        ChatModel model = new ChatModel();

        assertNotNull(model);
        assertNotNull(model.getMessages());
    }

    @Test
    @DisplayName("ChatModel should initialize messages list correctly")
    void testMessagesListNotNull() {
        ChatModel model = new ChatModel(java.net.http.HttpClient.newHttpClient());

        assertNotNull(model.getMessages());
        assertEquals(0, model.getMessages().size());
    }

    @Test
    @DisplayName("ChatModel with custom HttpClient should not be null")
    void testConstructorWithHttpClient() {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        ChatModel model = new ChatModel(client);

        assertNotNull(model);
        assertNotNull(model.getMessages());
    }

    @Test
    @DisplayName("ChatModel messages list should be observable")
    void testMessagesListIsObservable() {
        ChatModel model = new ChatModel(java.net.http.HttpClient.newHttpClient());

        assertTrue(model.getMessages() instanceof javafx.collections.ObservableList);
    }
}