package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class HelloModelTest {

    @Test
    void info_returnsCorrectUrl() {
        HelloModel model = new HelloModel();
        assertEquals("https://ntfy.sh/java25-erika-chat", model.info());
    }

    @Test
    void sendMessage_doesNotThrow() {
        HelloModel model = new HelloModel();
        assertDoesNotThrow(() -> model.sendMessage("test"));
    }

    @Test
    void startListening_doesNotThrow() {
        HelloModel model = new HelloModel();
        assertDoesNotThrow(() -> model.startListening(msg -> {}, err -> {}));
    }
}
