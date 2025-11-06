package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @Test
    @DisplayName("GIVEN a model with messageToSend WHEN calling sendMessage THEN send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {
        // Arrange  - Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        // Act      - When
        model.sendMessage();
        // Assert   - Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("GIVEN a fake Ntfy server WHEN calling sendMessage THEN an HTTP POST request should be sent with correct body")
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
}