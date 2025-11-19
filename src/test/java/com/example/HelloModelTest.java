package com.example;


import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    // Initiera JavaFX Toolkit en gång för alla tester eftersom HelloModel använder Platform.runLater
    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit är redan startat, ignorera
        }
    }

    @Test
    @DisplayName("When calling sendMessage it should call connection send")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange   Given
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);
        model.setMessageToSend("Hello World");
        //Act   When
        model.sendMessage();
        //Assert    Then
        assertThat(spy.message()).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("When connection receives a message, the model list should be updated")
    void incomingMessageAddsToModelList() throws InterruptedException {
        // Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        // Verifiera att modellen började lyssna
        assertThat(spy.wasReceiveCalled()).isTrue();

        var incomingMessage = new NtfyMessageDto("1", 1000L, "message", "test", "Incoming Data");

        // Act - Simulera inkommande meddelande från "nätverket"
        // Vi behöver en Latch för att vänta på Platform.runLater i modellen
        CountDownLatch latch = new CountDownLatch(1);

        spy.triggerReceive(incomingMessage);

        // Vänta tills JavaFX-tråden har bearbetat uppdateringen
        Platform.runLater(latch::countDown);

        boolean processed = latch.await(2, TimeUnit.SECONDS);
        assertThat(processed).as("JavaFX processing timed out").isTrue();

        // Assert
        assertThat(model.getMessages()).hasSize(1);
        assertThat(model.getMessages().getFirst().message()).isEqualTo("Incoming Data");
    }

    @Test
    void sendMessageToFakeServer(WireMockRuntimeInfo wireMockRuntimeInfo) {
        stubFor(get(urlPathMatching("/mytopic/json.*")).willReturn(ok()));

        stubFor(post("/mytopic").willReturn(ok()));

        var con = new NtfyConnectionImpl("http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);
        model.setMessageToSend("Hello World");

        model.sendMessage();

        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(containing("Hello World")));
    }

    @Test
    @DisplayName("Real NtfyConnectionImpl should parse JSON correctly from WireMock")
    void realConnectionParsesJson(WireMockRuntimeInfo wm) throws InterruptedException {
        // Arrange
        String jsonResponse = """
                {"id":"123","time":1640995200,"event":"message","topic":"mytopic","message":"Hello from WireMock"}
                """;

        stubFor(get(urlEqualTo("/mytopic/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonResponse)));

        // Vi använder NtfyConnectionImpl direkt här för att isolera parsing-logiken från Modellen
        var con = new NtfyConnectionImpl("http://localhost:" + wm.getHttpPort());

        AtomicReference<NtfyMessageDto> receivedDto = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Act
        con.receiveMessage(dto -> {
            receivedDto.set(dto);
            latch.countDown();
        });

        // Assert
        boolean received = latch.await(5, TimeUnit.SECONDS);
        assertThat(received).as("Did not receive message from stream").isTrue();

        assertThat(receivedDto.get()).isNotNull();
        assertThat(receivedDto.get().message()).isEqualTo("Hello from WireMock");
        assertThat(receivedDto.get().time()).isEqualTo(1640995200L);
    }
}