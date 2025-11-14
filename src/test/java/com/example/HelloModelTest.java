package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
public class HelloModelTest {

    @Test
    @DisplayName("When calling sendMessage it should call connection send")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        //Act
        model.sendMessage("Hello World");

        //Assert
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wireMockRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);

        stubFor(post("JUV25D").willReturn(ok()));

        model.sendMessage("Hello World");

        //verify
        verify(postRequestedFor(urlEqualTo("/JUV25D")).withRequestBody(containing("Hello World")));

    }

}
