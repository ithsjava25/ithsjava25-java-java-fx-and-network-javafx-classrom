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
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {

        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        model.setMessageToSend("Hello World!");
        model.sendMessage();

        assertThat(spy.message).endsWith("Hello World!");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World!");
        stubFor(post("/mytopic").willReturn(ok()));

        model.sendMessage();
        verify(postRequestedFor(urlEqualTo("/mytopic")));
    }
}