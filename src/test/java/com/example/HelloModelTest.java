package com.example;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


@WireMockTest
class HelloModelTest {

    /**
     * Initierar JavaFX Toolkit en gång innan alla tester i klassen.
     */
    @BeforeAll
    public static void initToolkit() {
        // Kontrollera om Toolkiten redan är igång för att undvika IllegalStateException
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // Toolkit är redan igång, ignorera
        }
    }

    @Test
    @DisplayName("Given a model with messageToSend when calling sendMessage then send")
    void sendMessageCallsConnectionWithMessagesToSend() {
        //Arrange - Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act - When
        model.sendMessage();
        //Assert - Then
        assertThat(spy.message).isEqualTo("Hello World");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wmRunTimeInfo) throws InterruptedException, ExecutionException {
        var con = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");
        stubFor(post("/mytopic").willReturn(ok()));

        var messageHolder = model.sendMessage();
        messageHolder.get();

        //Verify call made to server
        WireMock.verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }

    // Test för att se att vi får meddelanden tillbaka. receiveMessage?
    @Test
    void checkReceivedMessagesAfterSendingAMessageToServer() throws InterruptedException {
        //Arrange
        var conImp = new NtfyConnectionImpl();
        var model = new HelloModel(conImp);
        model.setMessageToSend("Hello World");
        //Act - When

        model.sendMessage();

        model.receiveMessage();

        Thread.sleep(1000);

        var messageList = model.getMessages();
        //Assert
        assertThat(messageList).extracting(NtfyMessageDto::message).contains("Hello World");
    }

    @Test
    void checkReceivedMessagesAfterSendingAMessageToAFakeServer(WireMockRuntimeInfo wmRunTimeInfo) throws InterruptedException {
        // Arrange
        var conImp = new NtfyConnectionImpl("http://localhost:" + wmRunTimeInfo.getHttpPort());

        //stubFor(get("/mytopic/json").willReturn(ok("{\"message\":\"hej\"}")));
        stubFor(get("/mytopic/json")
                .willReturn(aResponse()
                        .withHeader("Content-type", "application/json")
                        .withBody("{\"event\": \"message\",\"message\": \"Hello World\", \"time\": \"12314244\"}")));

        var model = new HelloModel(conImp);

        Thread.sleep(1000);

        assertThat(model.getMessages().getLast().message()).isEqualTo("Hello World");

    }

    // Test som skickar in ett fake:at meddelande via record och kollar att meddelandet finns i observablelistan
    @Test
    void checkThatReceivedFakeMessageAppearInList() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        //Skapa ett meddelande genom en record och skicka den till listan
        var fakeMessage = new NtfyMessageDto("id1", 1746598362, "message", "fmtopic", "Hallå");
        spy.simulateIncomingMessage(fakeMessage);

        // kontrollera att Meddelandet finns i listan
        assertThat(model.getMessages()).extracting(NtfyMessageDto::message).contains("Hallå");

    }
}