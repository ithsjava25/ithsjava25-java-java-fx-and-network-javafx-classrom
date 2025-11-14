package com.example;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
public class HelloModelTest {

    @BeforeAll
    static void initFx() {
        // Initialize JavaFX Toolkit for testing
        Platform.startup(() -> {});
    }


    // ---------------------------------------------------------------
    // 1. Enkel enhetstest – modell logik med spy
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Given messageToSend, sendMessage calls connection.send(msg)")
    void sendMessageCallsConnection() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        model.setMessageToSend("Hello World");
        model.sendMessage(model.messageToSendProperty().get());

        assertThat(spy.lastSentMessage).isEqualTo("Hello World");
    }

    // ---------------------------------------------------------------
    // 2. Enkel mottagning – också spy
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Incoming message via spy should be added to model messages")
    void receiveMessagesWithSpy() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        spy.simulateIncomingMessage("{\"message\":\"Hej från test\"}");

        assertThat(model.getMessages())
                .extracting("message")
                .contains("Hej från test");
    }

    // ---------------------------------------------------------------
    // 3. Test som verifierar verklig HTTP via NtfyConnectionImpl + WireMock
    // ---------------------------------------------------------------
    @Test
    @DisplayName("sendMessage sends POST request to WireMock server")
    void sendMessageToWireMock(WireMockRuntimeInfo wireMockRuntimeInfo) {

        WireMock.stubFor(post("/" + HelloModel.DEFAULT_TOPIC).willReturn(ok()));

        var con = new NtfyConnectionImpl("http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con);

        model.setMessageToSend("Hello WireMock");
        model.sendMessage(model.messageToSendProperty().get());

        verify(postRequestedFor(urlEqualTo("/" + HelloModel.DEFAULT_TOPIC))
                .withRequestBody(matching("Hello WireMock")));
    }

    // ---------------------------------------------------------------
    // 4. Integrations-test: upload + skicka bild → WireMock verifierar
    // ---------------------------------------------------------------
    @Test
    @DisplayName("sendImage uploads file and sends URL via WireMock server")
    void sendImageIntegratesWithWireMock(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {

        String baseUrl = "http://localhost:" + wireMockRuntimeInfo.getHttpPort();

        // Simulerat uppladdnings-endpoint (returnerar bild-URL)
        WireMock.stubFor(post("/upload")
                .willReturn(ok(baseUrl + "/images/test.png")));

        // Stub för notification endpoint
        WireMock.stubFor(post("/MartinsTopic").willReturn(ok()));

        // Använd riktig implementation → HTTP sker på riktigt
        var con = new NtfyConnectionImpl(baseUrl);

        // Subclass endast för att styra fil-URL (ingen nätverk här)
        HelloModel model = new HelloModel(con) {
            @Override
            protected String uploadToLocalServer(File imageFile) {
                return baseUrl + "/images/" + imageFile.getName();
            }
        };

        File testFile = File.createTempFile("test", ".png");
        testFile.deleteOnExit();

        boolean result = model.sendImage(testFile);

        assertThat(result).isTrue();

        verify(postRequestedFor(urlEqualTo("/MartinsTopic"))
                .withRequestBody(matching(".*" + testFile.getName() + ".*")));
    }
}
