package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@WireMockTest
class HelloModelTest {

    @Test
    @DisplayName("Model should pass the text to the connection when sending a message")
    void modelDelegatesSendToConnection() {
        var fakeConnection = new NtfyConnectionSpy();
        var model = new HelloModel(fakeConnection);

        model.setMessageToSend("Ping!");
        model.sendMessage();

        assertThat(fakeConnection.message)
                .as("Message should be forwarded to NtfyConnection")
                .isEqualTo("Ping!");
    }

    @Test
    @DisplayName("Integration: sending a message should hit /mytopic on a mock server")
    void sendMessageReachesMockServer(WireMockRuntimeInfo wm) {
        var baseUrl = "http://localhost:" + wm.getHttpPort();
        var con = new NtfyConnectionImpl(baseUrl);
        var model = new HelloModel(con);

        stubFor(post("/mytopic").willReturn(aResponse().withStatus(200)));

        model.setMessageToSend("Hello from test");
        model.sendMessage();

        verify(postRequestedFor(urlPathEqualTo("/mytopic"))
                .withRequestBody(matching(".*Hello from test.*")));
    }

    @Test
    @DisplayName("Model should contain an initial message after construction")
    void initialMessagesListContainsWelcomeMessage() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        assertThat(model.getMessages())
                .as("Model should initialize with one default message")
                .hasSize(1);
    }

    @Test
    @DisplayName("Message field should reset to empty after successful send")
    void messageFieldClearsAfterSend() {
        var dummyConnection = new NtfyConnectionSpy();
        var model = new HelloModel(dummyConnection);

        model.setMessageToSend("Reset me!");
        model.sendMessage();

        assertThat(model.getMessageToSend())
                .as("Message input should be cleared after send")
                .isEmpty();
    }

    @Test
    @DisplayName("Sending blank or empty text should not call the connection")
    void emptyMessageShouldNotBeSent() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        model.setMessageToSend("   ");
        model.sendMessage();

        assertThat(spy.message)
                .as("Connection should not be called with blank input")
                .isNullOrEmpty();
    }
}

