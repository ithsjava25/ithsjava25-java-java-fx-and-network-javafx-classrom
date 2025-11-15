package java.com.example.chat.model;

package com.example.chat.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChatModelTest {
    @Test
    void buildPublishJson_shouldContainTopicAndMessage() throws Exception {
        ChatModel m = new ChatModel();
        String json = m.buildPublishJson("hejtopic", "hej världen");
        assertTrue(json.contains("hejtopic"));
        assertTrue(json.contains("hej världen"));
    }

    @Test
    void addMessage_addsToList() {
        ChatModel m = new ChatModel();
        m.addMessage(new ChatMessage("Anna", "Hej"));
        assertEquals(1, m.getMessages().size());
    }
}
