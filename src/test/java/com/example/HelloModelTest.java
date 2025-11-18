package com.example;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HelloModelTest {

    @Test
    void testGetMessages_ReturnsEmptyListInitially() {
            HelloModel model = new HelloModel();
            assertThat(model.getMessages()).isEmpty();
    }


        @Test
        void testAddMessage_AddsMessageToList() {
            HelloModel model = new HelloModel();
            model.setTesting(true); //Aktivera testläge
            NtfyMessage testMessage = new NtfyMessage(
                    "123",
                    System.currentTimeMillis() / 1000,
                    "message",
                    "mytopic",
                    "Testmeddelande"
            );
            model.addMessage(testMessage);
            assertThat(model.getMessages())
                    .hasSize(1)
                    .containsExactly(testMessage);


        }

}
