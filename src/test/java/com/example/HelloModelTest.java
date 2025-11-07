package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest
public class HelloModelTest {

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called.")
    void sendMessageCallsConnectionWithMessageToSend(){
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
    void sendMessageToFakeServer (WireMockRuntimeInfo wireMockRuntimeInfo) {
        var con = new NtfyConnectionImpl("Http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        WireMock.stubFor(post("/MartinsTopic").willReturn(ok()));

        model.sendMessage();
        //Verify call med to server.
        verify(postRequestedFor(urlEqualTo("/MartinsTopic"))
                .withRequestBody(matching("Hello World")));
    }
}
