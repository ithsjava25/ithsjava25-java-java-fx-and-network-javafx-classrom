package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HelloModelTest {

    @Test
    void testModelInitializationWithEnv() {
        try {
            HelloModel model = new HelloModel();
            assertNotNull(model, "Model should be created successfully with .env");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("BACKEND_URL"), "Exception should mention BACKEND_URL");
        }
    }

    @Test
    void testParseIncomingLineAddsEmoji() {
        HelloModel model = new HelloModel("test-topic", "https://ntfy.sh");
        String input = "{\"message\":\"Hej\"}";
        String result = model.parseIncomingLine(input);
        assertTrue(result.startsWith("💬 "), "Meddelandet ska börja med emoji");
        assertTrue(result.contains("Hej"), "Meddelandet ska innehålla originaltexten");
    }

    @Test
    void testSendMessageDoesNotThrow() {
        HelloModel model = new HelloModel("test-topic", "https://ntfy.sh");
        assertDoesNotThrow(() -> model.sendMessage("Testmeddelande"));
    }
}
