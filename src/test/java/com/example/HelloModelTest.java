package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class HelloModelTest {

    @Test
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange
        var spy = new NtfyConnectionSpy();
                var model = new HelloModel(spy);
                //Act
                model.setMessageToSend("");

                model.sendMessage("Hello World");
//Assert
                assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("");
        stubFor(post("/catChat").willReturn(ok()));

        model.sendMessage("Hello World");

        WireMock.verify(postRequestedFor(urlEqualTo("/catChat"))
                .withRequestBody(matching("Hello World")));
    }

    @Test
    void messageIsAddedToObservableList() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        var testText = new NtfyMessageDto("id1", 143275436L,"Message", "catChat", "Godmorgon");
        spy.simulateIncomingMessage(testText);

        assertThat(model.getMessages()).extracting(NtfyMessageDto::message).contains("Godmorgon");
    }


}