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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@WireMockTest
class HelloModelTest {

    private static final String DEFAULT_TOPIC = "general";


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
                "message");

        assertThat(model.getMessages()).isEmpty();

        //Act  When
        spy.messageHandler.accept(testDto);
        //Assert   Then
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().get(0).message().equals(testMessage));
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
        assertThat(spy.fileTopicSent).isEqualTo(DEFAULT_TOPIC);
        tempFile.delete();
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

    @Test
    @DisplayName("reconnectToTopic should update model topic and call connect on connection")
    void reconnectToTopic_updatesTopicAndConnects() {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        String newTopic = "test-channel";

        // Kontrollera initial state
        assertThat(model.currentTopicProperty().get()).isEqualTo(DEFAULT_TOPIC);

        // Act
        model.reconnectToTopic(newTopic);

        // Assert
        assertThat(model.currentTopicProperty().get()).isEqualTo(newTopic);
        assertThat(spy.getTopic()).isEqualTo(newTopic);
        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("reconnectToTopic should not run if new topic is the same as current topic")
    void reconnectToTopic_noOpIfSameTopic() {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        String initialTopic = model.currentTopicProperty().get();
        String initialTopicFromSpy = spy.getTopic();

        spy.messageHandler.accept(new NtfyMessageDto("id", 123, "message", initialTopic, "Test message"));
        assertThat(model.getMessages()).hasSize(1);

        spy.messageSent = "Constructor called";

        // Act
        model.reconnectToTopic(initialTopic);

        // Assert

        assertThat(model.currentTopicProperty().get()).isEqualTo(initialTopic);
        assertThat(spy.getTopic()).isEqualTo(initialTopicFromSpy);
        assertThat(model.getMessages()).hasSize(1);
        assertThat(spy.messageSent).isEqualTo("Constructor called");
    }



}