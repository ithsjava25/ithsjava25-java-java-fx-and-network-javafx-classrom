package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act When
        model.sendMessage();
        //Assert Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));

        model.sendMessage();

        //Verify call made to server
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }
    @Test
    @DisplayName("When message is received then model should update received message")
    void receivedMessageUpdatesModel(WireMockRuntimeInfo wmRuntimeInfo) throws InterruptedException {
        //Arrange - Given
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());

        AtomicReference<String> receivedMessage = new AtomicReference<>();

        //Mock ntfy Server-Sent Events
        stubFor(get("/mytopic/json")
                .willReturn(ok()
                        .withHeader("Content-Type", "text/event-stream")
                        .withBody("data: {\"id\":\"123\",\"time\":1630000000,\"event\":\"message\",\"topic\":\"mytopic\",\"message\":\"Hello from test\"}\n\n")));

        //Act When
        var model = new HelloModel(con);

        //Register callback after model created
        model.setOnMessageReceived(msg -> {
            System.out.println("Callback called with message: " + msg);
            receivedMessage.set(msg);
        });

        //Wait 2 seconds for message
        Thread.sleep(2000);

        //Assert Then
        assertThat(receivedMessage.get()).isEqualTo("Hello from test");
    }
}