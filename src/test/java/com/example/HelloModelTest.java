package com.example;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class HelloModelTest {

    private static final String DEFAULT_TOPIC = "mytopic";

    // I HelloModelTest.java
    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection is called")
    void sendMessageCallsConnectionWithMsgToSend() {
        // Arrange - Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        String expectedMessage = "Hello World";
        model.setMessageToSend(expectedMessage);

        // Act - When
        model.sendMessage();

        // Assert - Then
        // 1. Verifiera att anslutningen anropades med rätt meddelande:
        assertThat(spy.getLastSentMessage()).isEqualTo(expectedMessage);

        // 2. Verifiera att det skickade meddelandet lades till i modellens lista (lokal uppdatering):
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0).message()).isEqualTo(expectedMessage);
        // Verifiera även att det markerades som lokalt skickat
        assertThat(model.getMessages().get(0).isLocal()).isTrue();
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        stubFor(post("/" + DEFAULT_TOPIC).willReturn(ok()));
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");


        model.sendMessage();

        //Verify call made to server
        verify(postRequestedFor(urlEqualTo("/" + DEFAULT_TOPIC))
                .withRequestBody(matching("Hello World")));
    }




    @Test
    void receiveMessageIsAddedToObservableList() {
        //Arrange  Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        var testMessage = "This is a test message";

        var testDto=new NtfyMessageDto(
                "id-123",
                System.currentTimeMillis(),
                "message",
                DEFAULT_TOPIC,
                testMessage);

        assertThat(model.getMessages()).isEmpty();

        //Act  When
        spy.messageHandler.accept(testDto);
        //Assert   Then
        assertThat(model.getMessages()).hasSize(1);
        // KORRIGERING: Använder NtfyMessageDto.message()
        assertThat(model.getMessages().get(0).message()).isEqualTo(testMessage);
    }



}