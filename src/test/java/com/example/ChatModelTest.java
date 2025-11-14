package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@WireMockTest
class ChatModelTest extends ApplicationTest {

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
        long expectedTimestamp = System.currentTimeMillis();

        stubFor(get("/mytopic/json")
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBody("{\"event\": \"message\",\"message\": \"" + expectedMessage + "\", \"time\": \"" + expectedTimestamp + "\"}")));

        var model = new ChatModel(conImp);
        model.startReceiving();

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                assertThat(model.getMessages()).isNotEmpty();
                assertThat(model.getMessages().getLast().content()).isEqualTo(expectedMessage);
                assertThat(model.getMessages().getLast().timestamp()).isEqualTo(expectedTimestamp);
            } catch (AssertionError e) {
                fail("Assertion failed inside Platform.runLater: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        assertThat(latch.await(5, TimeUnit.SECONDS))
                .as("Timed out waiting for JavaFX thread to execute assertions.")
                .isTrue();
    }
}