package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
    void receiveMessageFromFakeServer(WireMockRuntimeInfo wireMockRuntimeInfo) throws IOException {
        //Arrange
        var host = "http://localhost:" + wireMockRuntimeInfo.getHttpPort();
        var con = new NtfyConnectionImpl(host);
        String fakeMessage = """
                {"id":"testID","time":1762935416, "event":"keepalive","topic":"catChat", "message":"Filtreras bort"}
                {"id":"testID","time":1762935416, "event":"message","topic":"catChat", "message":"User: Hej"}""";

        //Simulerar en server
        stubFor(get(urlEqualTo("/catChat/json")).willReturn(aResponse()
                        .withStatus(200)
                .withBody(fakeMessage)));

        Consumer<NtfyMessageDto> fakeReceiver = Mockito.mock(Consumer.class);
        //con.receive(fakeReceiver);

        ArgumentCaptor<NtfyMessageDto> captor = ArgumentCaptor.forClass(NtfyMessageDto.class);
        Subscription subscription = con.receive(fakeReceiver);
        //Act

        Awaitility.await()
                .atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
                Mockito.verify(fakeReceiver, Mockito.times(1))
                        .accept(captor.capture());
                });

        //Assert
        String messageReceived = captor.getValue().message();
        assertThat(messageReceived).isEqualTo("User: Hej");

        subscription.close(); //Stänger anslutningen
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

        var testText = new NtfyMessageDto("id1", 15465823L,"Message", "catChat", "Godmorgon");
        spy.simulateIncomingMessage(testText);

        assertThat(model.getMessages()).extracting(NtfyMessageDto::message).contains("Godmorgon");
    }


}