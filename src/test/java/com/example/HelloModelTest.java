package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WireMockTest
class HelloModelTest {

    @Test
    @DisplayName("sendMessage should call connection with correct JSON")
    void sendMessageCallsConnectionWithMessageToSend() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        String message = "Hello World";

        model.sendMessage(message);

        assertThat(spy.lastSentMessage)
                .contains("\"message\":\"Hello World\"")
                .contains("\"clientId\":\"" + model.getClientId() + "\"");
    }

    @Test
    @DisplayName("sendImage should forward clientId and file to connection")
    void sendImage_shouldPassClientIdToConnection() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        File dummyImage = new File("test-image.jpg");
        boolean result = model.sendImage(dummyImage);

        assertTrue(result);
        assertEquals(dummyImage, spy.lastSentImage);
        assertEquals(model.getClientId(), spy.lastClientId);
    }

    @Test
    @DisplayName("incoming messages with same clientId should be ignored")
    void shouldIgnoreOwnTextAndImageMessages() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        String clientId = model.getClientId();

        // egna textmeddelanden
        var ownText = new NtfyMessageDto("1", System.currentTimeMillis(), "message",
                "MartinsTopic", null, null, null);
        spy.simulateIncomingMessage("{\"clientId\":\"" + clientId + "\",\"message\":\"hej\"}");

        // egna bildmeddelanden
        spy.simulateIncomingMessage("{\"clientId\":\"" + clientId + "\",\"imageUrl\":\"some-url.jpg\"}");

        // meddelande från annan användare
        spy.simulateIncomingMessage("{\"clientId\":\"other-id\",\"message\":\"Hej från annan\"}");

        assertEquals(1, model.getMessages().size());
        assertThat(model.getMessages().get(0).message()).contains("Hej från annan");
    }

    @Test
    @DisplayName("receiveMessage should add external text messages to model")
    void receiveMessageAddsToMessages() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        // simulera inkommande meddelande från annan användare
        spy.simulateIncomingMessage("{\"clientId\":\"other-id\",\"message\":\"Hej från servern!\"}");

        assertThat(model.getMessages())
                .hasSize(1)
                .first()
                .extracting(NtfyMessageDto::message)
                .isEqualTo("Hej från servern!");
    }
}
