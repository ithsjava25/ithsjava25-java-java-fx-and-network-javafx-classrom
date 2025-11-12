package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @BeforeAll
    static void setupJavaFX() {
        if (!Platform.isFxApplicationThread()) {
            Platform.startup(() -> {});
        }
    }

    //successful send tests
    @Test
    void shouldSendMessageThroughConnection() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);
        model.setMessageToSend("Hello World");

        CountDownLatch latch = new CountDownLatch(1);
        model.sendMessageAsync(success -> latch.countDown());

        assertThat(latch.await(500, TimeUnit.MILLISECONDS)).isTrue();
        assertThat(connectionSpy.message).isEqualTo("Hello World");
    }

    @Test
    void shouldHandleMultipleConsecutiveSends() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);

        CountDownLatch latch = new CountDownLatch(2);
        boolean[] results = new boolean[2];

        model.setMessageToSend("First");
        model.sendMessageAsync(success -> {
            results[0] = success;
            latch.countDown();
        });

        model.setMessageToSend("Second");
        model.sendMessageAsync(success -> {
            results[1] = success;
            latch.countDown();
        });

        latch.await(1, TimeUnit.SECONDS);
        assertThat(results[0]).isTrue();
        assertThat(results[1]).isTrue();
        assertThat(connectionSpy.message).isEqualTo("Second");
    }

    //invalid send tests
    @Test
    void shouldRejectBlankMessages() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);

        String[] invalidInputs = {"", null};

        for (String input : invalidInputs) {
            model.setMessageToSend(input);
            CountDownLatch latch = new CountDownLatch(1);
            boolean[] wasSuccessful = new boolean[1];

            model.sendMessageAsync(success -> {
                wasSuccessful[0] = success;
                latch.countDown();
            });

            latch.await(500, TimeUnit.MILLISECONDS);
            assertThat(wasSuccessful[0]).isFalse();
            assertThat(connectionSpy.message).isNull();
        }
    }

    @Test
    void shouldFailWhenSendingEmptyText() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);
        model.setMessageToSend("");

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] wasSuccessful = new boolean[1];

        model.sendMessageAsync(success -> {
            wasSuccessful[0] = success;
            latch.countDown();
        });

        latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(wasSuccessful[0]).isFalse();
        assertThat(connectionSpy.message).isNull();
    }

    @Test
    void shouldFailWhenSendingNullMessage() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);
        model.setMessageToSend(null);

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] wasSuccessful = new boolean[1];

        model.sendMessageAsync(success -> {
            wasSuccessful[0] = success;
            latch.countDown();
        });

        latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(wasSuccessful[0]).isFalse();
        assertThat(connectionSpy.message).isNull();
    }

    //error handling tests
    @Test
    void shouldReturnFailureWhenConnectionFails() throws InterruptedException {
        NtfyConnection failingConn = new NtfyConnection() {
            @Override
            public boolean send(String message) {
                return false;
            }
            @Override
            public void receive(Consumer<NtfyMessageDto> messageHandler) { }
        };
        HelloModel model = new HelloModel(failingConn);
        model.setMessageToSend("Fail this message");

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] wasSuccessful = new boolean[1];

        model.sendMessageAsync(success -> {
            wasSuccessful[0] = success;
            latch.countDown();
        });

        latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(wasSuccessful[0]).isFalse();
        assertThat(model.getMessageToSend()).isEqualTo("Fail this message");
    }

    @Test
    void shouldHandleExceptionsDuringSend() throws InterruptedException {
        NtfyConnection throwingConn = new NtfyConnection() {
            @Override
            public boolean send(String message) {
                throw new RuntimeException("Simulated crash");
            }
            @Override
            public void receive(Consumer<NtfyMessageDto> messageHandler) { }
        };
        HelloModel model = new HelloModel(throwingConn);
        model.setMessageToSend("Crash this");

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] wasSuccessful = new boolean[1];

        try {
            model.sendMessageAsync(success -> {
                wasSuccessful[0] = success;
                latch.countDown();
            });
        } catch (Exception ex) {
            wasSuccessful[0] = false;
            latch.countDown();
        }

        latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(wasSuccessful[0]).isFalse();
    }

    //receiving messages tests
    @Test
    void shouldAddIncomingMessageToList() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);
        NtfyMessageDto incomingMsg = new NtfyMessageDto("Test", 1, "message", "myroom", "Test");

        CountDownLatch latch = new CountDownLatch(1);
        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    latch.countDown();
                }
            }
        });

        connectionSpy.simulateIncoming(incomingMsg);

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(model.getMessages()).contains(incomingMsg);
    }
    @Test
    void shouldDiscardNullIncomingMessage() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);

        CountDownLatch latch = new CountDownLatch(1);
        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) c -> {
            while (c.next()) {
                if (c.wasAdded()) latch.countDown();
            }
        });

        connectionSpy.simulateIncoming(null);

        boolean messageAdded = latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(messageAdded).isFalse();
        assertThat(model.getMessages()).isEmpty();
    }
    @Test
    void shouldIgnoreMessagesWithBlankContent() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);

        CountDownLatch latch = new CountDownLatch(1);
        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) c -> {
            while (c.next()) {
                if (c.wasAdded()) latch.countDown();
            }
        });

        NtfyMessageDto whitespaceMsg = new NtfyMessageDto("id1", 1, "message", "room", "   ");
        NtfyMessageDto emptyMsg = new NtfyMessageDto("id2", 2, "message", "room", "");

        connectionSpy.simulateIncoming(whitespaceMsg);
        connectionSpy.simulateIncoming(emptyMsg);

        boolean messageAdded = latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(messageAdded).isFalse();
        assertThat(model.getMessages()).isEmpty();
    }
    @Test
    void shouldRejectAllInvalidIncomingMessages() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);

        CountDownLatch latch = new CountDownLatch(1);
        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) c -> {
            while (c.next()) {
                if (c.wasAdded()) latch.countDown();
            }
        });

        connectionSpy.simulateIncoming(new NtfyMessageDto("id1", 1, "message", "room", ""));
        connectionSpy.simulateIncoming(new NtfyMessageDto("id2", 2, "message", "room", "  "));
        connectionSpy.simulateIncoming(null);

        boolean messageAdded = latch.await(500, TimeUnit.MILLISECONDS);
        assertThat(messageAdded).isFalse();
        assertThat(model.getMessages()).isEmpty();
    }
    //integration test
    @Test
    void shouldCommunicateWithMockedServer(WireMockRuntimeInfo wmInfo) throws InterruptedException {
        NtfyConnectionImpl connection = new NtfyConnectionImpl("http://localhost:" + wmInfo.getHttpPort());
        HelloModel model = new HelloModel(connection);
        model.setMessageToSend("Hello World");

        stubFor(post("/mytopic").willReturn(ok()));
        stubFor(get("/mytopic/json").willReturn(ok()));

        CountDownLatch latch = new CountDownLatch(1);
        model.sendMessageAsync(success -> latch.countDown());

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }
}