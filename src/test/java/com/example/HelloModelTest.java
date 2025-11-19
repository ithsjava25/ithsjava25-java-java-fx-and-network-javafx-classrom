package com.example;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @Test
    @DisplayName("When calling sendMessage it should call connection send")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange   Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act   When
        model.sendMessage();
        //Assert    Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wireMockRuntimeInfo) {
        stubFor(get(urlPathMatching("/mytopic/json.*")).willReturn(ok()));

        stubFor(post("/mytopic").willReturn(ok()));

        var con = new NtfyConnectionImpl("http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");

        model.sendMessage();

        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(containing("Hello World")));
    }
}