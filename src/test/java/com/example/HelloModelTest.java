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
        String message = "Hello World";
        model.setMessageToSend("Hello World");
        //Act   When
        model.sendMessage(message);
        //Assert    Then
        assertThat(spy.message).isEqualTo("Hello World");

    }

    @Test
    void sendMessageToFakeServer (WireMockRuntimeInfo wireMockRuntimeInfo) {
        var con = new NtfyConnectionImpl("Http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        String message = "Hello World";
        model.sendMessage(message);
        WireMock.stubFor(post("/MartinsTopic").willReturn(ok()));

        model.sendMessage("Hello World");
        //Verify call med to server.
        verify(postRequestedFor(urlEqualTo("/MartinsTopic"))
                .withRequestBody(matching("Hello World")));
    }
    @Test
    @DisplayName("Given a model when a message is received then it should be added to the messages list")
    void receiveMessageAddsToMessages() {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        // Act – simulera att ett meddelande tas emot
        spy.simulateIncomingMessage("Hej från servern!");

        // Assert – kontrollera att det lades till i modellens lista
        assertThat(model.getMessages())
                .hasSize(1)
                .first()
                .extracting(NtfyMessageDto::message)
                .isEqualTo("Hej från servern!");
    }
    @Test
    @DisplayName("Given a model when a message is received then it should be added to the message list")
    void receiveMessageAddsMessageToList() {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        // Act
        spy.simulateIncomingMessage("Hej från servern!");

        // Assert
        assertThat(model.getMessages())
                .hasSize(1)
                .extracting("message")
                .contains("Hej från servern!");
    }


}
