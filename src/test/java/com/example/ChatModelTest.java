package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.awaitility.Awaitility;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class ChatModelTest {

    @Test
    void sendMessageCallsConnectionWithMessagesToSend() {
        var spy = new NtfyConnectionSpy();
        var model = new ChatModel(spy);
        String messageToSend = "Test Message 123";

        model.sendMessage(messageToSend);

        assertThat(spy.message).isEqualTo(messageToSend);
    }

    @Test
    void sendMessageToFakeServer_viaNtfyConnectionImpl(WireMockRuntimeInfo wmRunTimeInfo) throws InterruptedException {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());
        var model = new ChatModel(con);
        String messageToSend = "Test Message 123";

        stubFor(post("/mytopic").willReturn(ok()));

        model.sendMessage(messageToSend);

        Thread.sleep(500);

        WireMock.verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching(messageToSend)));
    }


    @Test
    void checkReceivedMessagesAfterSendingAMessageToAFakeServer(WireMockRuntimeInfo wmRunTimeInfo) throws InterruptedException {
        var conImp = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());

        String expectedMessage = "Async Data Received";
        // Använd System.currentTimeMillis() om din ChatModel skickar detta;
        // annars, använd ett statiskt värde för att vara säker.
        long expectedTimestamp = System.currentTimeMillis();

        stubFor(get("/mytopic/json")
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBody("{\"event\": \"message\",\"message\": \"" + expectedMessage + "\", \"time\": \"" + expectedTimestamp + "\"}")));

        var model = new ChatModel(conImp);
        model.startReceiving();

        // Awaitility: Väntar aktivt tills listan fylls.
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> model.getMessages().size() > 0);

        assertThat(model.getMessages()).isNotEmpty();
        assertThat(model.getMessages().getLast().content()).isEqualTo(expectedMessage);
        assertThat(model.getMessages().getLast().timestamp()).isEqualTo(expectedTimestamp);
    }
}