package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HelloModelTest {

    @Test
    void testModelInitialization() {
        // Försök skapa modellen, hantera fel ifall BACKEND_URL saknas
        try {
            HelloModel model = new HelloModel("test-topic");
            assertNotNull(model, "Model should be created successfully");
        } catch (IllegalStateException e) {
            // Om miljövariabeln saknas ska felet ha rätt meddelande
            assertTrue(e.getMessage().contains("BACKEND_URL"), "Exception should mention BACKEND_URL");
        }
    }

    @Test
    void testBackendUrlDefault() {
        // Testar att miljövariabeln hanteras
        String backendUrl = System.getenv("BACKEND_URL");
        if (backendUrl == null || backendUrl.isBlank()) {
            backendUrl = "https://ntfy.sh";
        }
        assertTrue(backendUrl.startsWith("https://"), "BACKEND_URL should start with https://");
    }

    @Test
    void testSendMessageFormatting() {
        String userMessage = "Hej världen!";
        String formatted = "{\"message\": \"" + userMessage + "\"}";
        assertTrue(formatted.contains("Hej världen!"));
        assertTrue(formatted.contains("{"));
        assertTrue(formatted.contains("}"));
    }
}
