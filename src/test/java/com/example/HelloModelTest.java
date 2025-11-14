package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
public class HelloModelTest {

    // ---------------------------------------------------------------
    // 1. Simple unit test – model logic with spy (headless)
    // ---------------------------------------------------------------
    @Test
    @DisplayName("sendMessage calls connection.send(msg)")
    void sendMessageCallsConnection() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy, true); // headless = true

        model.setMessageToSend("Hello World");
        model.sendMessage(model.messageToSendProperty().get());

        assertThat(spy.lastSentMessage).isEqualTo("Hello World");
    }

    // ---------------------------------------------------------------
    // 2. Receiving messages via spy
    // ---------------------------------------------------------------
    @Test
    @DisplayName("Incoming text via spy is added to model messages")
    void receiveMessagesWithSpy() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy, true);

        spy.simulateIncomingMessage("Hello from test", null, "other-client");

        assertThat(model.getMessages())
                .extracting(NtfyMessageDto::message)
                .contains("Hello from test");
    }

    @Test
    @DisplayName("Incoming image via spy is added to model messages")
    void receiveImageWithSpy() {
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy, true);

        spy.simulateIncomingMessage(null, "http://example.com/test.png", "other-client");

        assertThat(model.getMessages())
                .extracting(NtfyMessageDto::imageUrl)
                .contains("http://example.com/test.png");
    }

    // ---------------------------------------------------------------
    // 3. WireMock test – sending text
    // ---------------------------------------------------------------
    @Test
    @DisplayName("sendMessage sends POST request to WireMock server")
    void sendMessageToWireMock(WireMockRuntimeInfo wireMockRuntimeInfo) {
        stubFor(post("/" + HelloModel.DEFAULT_TOPIC).willReturn(ok()));

        var con = new NtfyConnectionImpl("http://localhost:" + wireMockRuntimeInfo.getHttpPort());
        var model = new HelloModel(con, true);

        model.setMessageToSend("Hello WireMock");
        model.sendMessage(model.messageToSendProperty().get());

        verify(postRequestedFor(urlEqualTo("/" + HelloModel.DEFAULT_TOPIC))
                .withRequestBody(matching("Hello WireMock")));
    }

    // ---------------------------------------------------------------
    // 4. Integration test: upload image + send notification → verify with WireMock
    // ---------------------------------------------------------------
    @Test
    @DisplayName("sendImage uploads file and sends URL via WireMock server")
    void sendImageIntegratesWithWireMock(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
        String baseUrl = "http://localhost:" + wireMockRuntimeInfo.getHttpPort();

        // Stub for upload endpoint
        stubFor(post("/upload").willReturn(ok(baseUrl + "/images/test.png")));

        // Stub for notification endpoint
        stubFor(post("/MartinsTopic").willReturn(ok()));

        var con = new NtfyConnectionImpl(baseUrl);

        // Override uploadToLocalServer for test → headless
        HelloModel model = new HelloModel(con, true) {
            @Override
            protected String uploadToLocalServer(File imageFile) {
                return baseUrl + "/images/" + imageFile.getName();
            }
        };

        File testFile = File.createTempFile("test", ".png");
        testFile.deleteOnExit();

        boolean result = model.sendImage(testFile);
        assertThat(result).isTrue();

        // Verify that notification POST contains the image URL in Markdown format
        verify(postRequestedFor(urlEqualTo("/MartinsTopic"))
                .withRequestBody(matching(".*!\\[Bild\\]\\(.*" + testFile.getName() + "\\).*")));
    }
}
