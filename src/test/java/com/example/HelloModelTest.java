package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @BeforeAll
    static void initJavaFx() {
        Platform.startup(() -> {
        });
    }

    @Test
    @DisplayName("GIVEN a model with messageToSend WHEN calling sendMessage THEN send method on connection should be called")
    void sendMessageCallsConnectionWithMessageToSend() {
        // Arrange  - Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        // Act      - When
        model.sendMessage();
        // Assert   - Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("GIVEN a fake Ntfy server WHEN calling sendMessage THEN an HTTP POST request should be sent with correct body")
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRuntimeInfo) {
        // Arrange  - Given
        var con = new NtfyConnectionImpl("http://localhost:" + wmRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));
        // Act      - When
        model.sendMessage();
        // Assert   - Then
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }

    @Test
    @DisplayName("GIVEN a stubbed connection WHEN receiving message THEN it should appear in the model's messages list")
    void receiveMessageAddsMessagesToList() throws InterruptedException {
        // Arrange - Given
        var stub = new NtfyConnectionStub();
        var model = new HelloModel(stub);
        // Act - When
        stub.simulateIncomingMessage(new NtfyMessageDto("1", System.currentTimeMillis(), "message", "mytopic", "Hello world"));
        Thread.sleep(50);
        // Assert - Then
        assertThat(model.getMessages())
                .extracting(NtfyMessageDto::message)
                .containsExactly("Hello world");
    }

    @Test
    @DisplayName("GIVEN a model with messages WHEN connecting to a new topic THEN old messages are cleared")
    void connectToTopicClearsMessages() {
        // Arrange - Given
        var stub = new NtfyConnectionStub();
        var model = new HelloModel(stub);
        // Act - When
        stub.simulateIncomingMessage(new NtfyMessageDto("1", System.currentTimeMillis(), "message", "mytopic", "Old message"));
        assertThat(model.getMessages()).hasSize(1);
        model.setTopic("newtopic");
        model.connectToTopic();
        // Assert - Then
        assertThat(model.getMessages()).isEmpty();
    }

}