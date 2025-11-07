package com.example;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
        model.sendMessage();
        //Assert   Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
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
    @Test
    void receiveMessageIsAddedToObservableList() {
        //Arrange  Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        var testDto=new NtfyMessageDto(
                "id-123",
                System.currentTimeMillis(),
                "This is a test message",
                "mytopic",
                "message");

        assertThat(model.getMessages()).isEmpty();

        //Act  When
        spy.messageHandler.accept(testDto);
        //Assert   Then
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0).message().equals("This is a test message"));
    }
    @Test
    void sendFileWithTempFile() throws FileNotFoundException {
        //Arrange  Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        File tempFile;
        try{
            tempFile=File.createTempFile("testFile", ".txt");
        } catch(IOException e){
            throw new RuntimeException("Could not create temporary File for test", e);
        }
        //Act  When
        model.sendFile(tempFile);
        //Assert   Then
        assertThat(spy.fileSent).isNotNull();
        assertThat(spy.fileSent).isEqualTo(tempFile);
        tempFile.delete();
    }


}