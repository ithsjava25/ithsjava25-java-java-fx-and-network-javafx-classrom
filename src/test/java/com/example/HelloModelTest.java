package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange  Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act  When
        model.sendMessage().join();
        //Assert   Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));

        model.sendMessage().join();

        //Verify call made to server
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }

    @Test
    @DisplayName("Given a registered message handler, when a message is simulated, then it should be received")
    void receiveShouldTriggerHandler() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        AtomicReference<NtfyMessageDto> received = new AtomicReference<>();
        spy.receive(received::set);

        NtfyMessageDto mockResponse = new NtfyMessageDto(
                "123",
                System.currentTimeMillis(),
                "message",
                "mytopic",
                "Hej från testet!",
                null
        );

        // Act
        spy.simulateIncomingMessage(mockResponse);

        // Assert
        assertThat(received.get()).isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("Given HelloModel with spy connection, when a message is simulated, then it should be added to messages list")
    void receiveShouldAddMessageToModel() {
        new javafx.embed.swing.JFXPanel();
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        NtfyMessageDto mockMessage = new NtfyMessageDto(
                "123",
                System.currentTimeMillis(),
                "message",
                "mytopic",
                "Hej från testet!",
                null
        );

        // Act
        spy.simulateIncomingMessage(mockMessage);

        // Assert
        assertThat(model.getMessages()).containsExactly(mockMessage);
    }


}