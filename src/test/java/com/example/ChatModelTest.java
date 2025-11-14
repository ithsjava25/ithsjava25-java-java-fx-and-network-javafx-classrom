package com.example;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.awaitility.Awaitility;
import javafx.application.Platform;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


@WireMockTest
class ChatModelTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
            Platform.setImplicitExit(false);
        } catch (IllegalStateException e) {
        }
    }


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
    void checkReceivedMessagesAfterSendingAMessageToAFakeServer(WireMockRuntimeInfo wmRunTimeInfo) {
        var conImp = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());

        String expectedMessage = "Async Data Received";
        long expectedTimestamp = System.currentTimeMillis();

        stubFor(get("/mytopic/json")
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBody("{\"event\": \"message\",\"message\": \"" + expectedMessage + "\", \"time\": \"" + expectedTimestamp + "\"}")));

        var model = new ChatModel(conImp);
        model.startReceiving();

        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    assertThat(model.getMessages()).isNotEmpty();

                    assertThat(model.getMessages().getLast().content()).isEqualTo(expectedMessage);

                    assertThat(model.getMessages().getLast().timestamp()).isEqualTo(expectedTimestamp);
                });
    }
}