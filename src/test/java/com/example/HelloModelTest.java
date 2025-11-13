package com.example;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
// OBS: Om du fortfarande får 'Toolkit not initialized', lägg till FxTestExtension här.
//@ExtendWith(ApplicationExtension.class)
@WireMockTest
class HelloModelTest {

    private static final String DEFAULT_TOPIC = "mytopic"; // Ändrad till "mytopic" för att matcha NtfyConnectionImpl

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange  Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act  When
        model.sendMessage();
        //Assert   Then
        assertThat(spy.message).isEqualTo("Hello World");
        assertThat(spy.topicSent).isEqualTo(DEFAULT_TOPIC);
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
                testMessage); // Använd testMessage här

        assertThat(model.getMessages()).isEmpty();

        //Act  When
        spy.messageHandler.accept(testDto);
        //Assert   Then
        assertThat(model.getMessages()).hasSize(1);
        // KORRIGERING: Använd getMessage().message() istället för equals(testMessage) på meddelandet
        assertThat(model.getMessages().get(0).message()).isEqualTo(testMessage);
    }



    @Test
    @DisplayName("Model should not call send on connection if message is empty or whitespace")
    void sendMessage_shouldNotSendIfMessageIsEmpty() {
        //Arrange  Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        model.setMessageToSend(null);

        //Act  When
        model.sendMessage();
        //Assert   Then
        assertThat(spy.messageSent).isNull();

        model.setMessageToSend(" ");

        model.sendMessage();

        assertThat(spy.messageSent).isNull();
        assertThat(spy.topicSent).isNull();


    }
}