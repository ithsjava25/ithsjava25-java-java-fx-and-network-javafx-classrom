package com.example;


import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest
class ChatModelTest {
    static {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {

        }
    }


    @Test
    @DisplayName("När sendMessage anropas, ska rätt meddelande skickas till anslutningen")
    void sendMessage_callsConnectionWithCorrectMessage() {

        var spy = new NtfyConnectionSpy();

        var model = new ChatModel(spy);
        String messageToSend = "Hello Ntfy World!";


        model.sendMessage(messageToSend);


        assertThat(spy.sentMessage).isEqualTo(messageToSend);
    }


    @Test
    @DisplayName("När ett meddelande tas emot via anslutningen, ska det läggas till i Modelns lista")
    void receiveMessage_addsToObservableList() throws Exception {

        var spy = new NtfyConnectionSpy();
        var model = new ChatModel(spy);

        model.startReceiving();

        long testTimestamp = System.currentTimeMillis() / 1000;
        String receivedText = "Ett inkommande chattmeddelande";

        spy.simulateMessageArrival(receivedText, testTimestamp);


        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();

        assertThat(model.getMessages()).hasSize(1);
        ChatMessage received = model.getMessages().get(0);

        assertEquals(receivedText, received.content());
        assertEquals(testTimestamp, received.timestamp());
    }

    @Test
    @DisplayName("sendMessage ska skicka en POST-förfrågan till WireMock-servern med rätt body")
    void sendMessageToFakeServer_viaNtfyConnectionImpl(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new ChatModel(con);
        String messageToSend = "Test Body Content";


        stubFor(post("/mytopic").willReturn(ok()));


        model.sendMessage(messageToSend);


        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching(messageToSend)));
    }
}