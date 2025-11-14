package com.example;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class HelloModelTest {

    @BeforeAll
    static void setupJavaFX() {
        try {
            if (!Platform.isFxApplicationThread()) {
                Platform.startup(() -> {
                });
            }
        } catch (IllegalStateException | UnsupportedOperationException e) {
            System.out.println("Headless environment detected – skipping JavaFX startup");
        }
    }

    // successful send tests
    @Test
    void shouldSendMessageThroughConnection() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);
        model.setMessageToSend("Hello World");

        CountDownLatch latch = new CountDownLatch(1);
        model.sendMessageAsync(success -> latch.countDown());

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for message send").isTrue();
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

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for consecutive sends").isTrue();
        assertThat(results[0]).isTrue();
        assertThat(results[1]).isTrue();
        assertThat(connectionSpy.message).isEqualTo("Second");
    }

    // invalid send tests
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

            boolean completed = latch.await(5, TimeUnit.SECONDS);
            assertThat(completed).as("Timed out waiting for blank message rejection").isTrue();
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

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for empty text rejection").isTrue();
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

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for null message rejection").isTrue();
        assertThat(wasSuccessful[0]).isFalse();
        assertThat(connectionSpy.message).isNull();
    }

    // error handling tests
    @Test
    void shouldReturnFailureWhenConnectionFails() throws InterruptedException {
        NtfyConnection failingConn = new NtfyConnection() {
            @Override
            public void send(String message, Consumer<Boolean> callback) {
                callback.accept(false);
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

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for connection failure").isTrue();
        assertThat(wasSuccessful[0]).isFalse();
        assertThat(model.getMessageToSend()).isEqualTo("Fail this message");
    }

    @Test
    void shouldHandleExceptionsDuringSend() throws InterruptedException {
        NtfyConnection throwingConn = new NtfyConnection() {
            @Override
            public void send(String message, Consumer<Boolean> callback) {
                throw new RuntimeException("Simulated crash");
            }

            @Override
            public void receive(Consumer<NtfyMessageDto> messageHandler) {
            }
        };
        HelloModel model = new HelloModel(throwingConn);
        model.setMessageToSend("Crash this");

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] wasSuccessful = new boolean[1];

        //wrappa i try-catch för att fånga exception från connection
        try {
            model.sendMessageAsync(success -> {
                wasSuccessful[0] = success;
                latch.countDown();
            });
        } catch (Exception e) {
            wasSuccessful[0] = false;
            latch.countDown();
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for exception handling").isTrue();
        assertThat(wasSuccessful[0]).isFalse();
    }

    // receiving messages tests
    @Test
    void shouldAddIncomingMessageToList() throws InterruptedException {
        NtfyConnectionSpy connectionSpy = new NtfyConnectionSpy();
        HelloModel model = new HelloModel(connectionSpy);
        NtfyMessageDto incomingMsg = new NtfyMessageDto("Test", 1, "message", "myroom", "Test");

        AtomicBoolean messageReceived = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    messageReceived.set(true);
                    latch.countDown();
                }
            }
        });

        //give the listener time to attach
        Thread.sleep(100);

        connectionSpy.simulateIncoming(incomingMsg);

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for incoming message").isTrue();
        assertThat(messageReceived.get()).isTrue();
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

        Thread.sleep(100);
        connectionSpy.simulateIncoming(null);

        boolean messageAdded = latch.await(2, TimeUnit.SECONDS);
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

        Thread.sleep(100);

        NtfyMessageDto whitespaceMsg = new NtfyMessageDto("id1", 1, "message", "room", "   ");
        NtfyMessageDto emptyMsg = new NtfyMessageDto("id2", 2, "message", "room", "");

        connectionSpy.simulateIncoming(whitespaceMsg);
        connectionSpy.simulateIncoming(emptyMsg);

        boolean messageAdded = latch.await(2, TimeUnit.SECONDS);
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

        Thread.sleep(100);

        connectionSpy.simulateIncoming(new NtfyMessageDto("id1", 1, "message", "room", ""));
        connectionSpy.simulateIncoming(new NtfyMessageDto("id2", 2, "message", "room", "  "));
        connectionSpy.simulateIncoming(null);

        boolean messageAdded = latch.await(2, TimeUnit.SECONDS);
        assertThat(messageAdded).isFalse();
        assertThat(model.getMessages()).isEmpty();
    }

    //integration test
    @Test
    void shouldCommunicateWithMockedServer(WireMockRuntimeInfo wmInfo) throws InterruptedException {
        stubFor(post("/mytopic").willReturn(ok()));
        stubFor(get("/mytopic/json").willReturn(ok().withBody("")));

        NtfyConnectionImpl connection = new NtfyConnectionImpl("http://localhost:" + wmInfo.getHttpPort());
        HelloModel model = new HelloModel(connection);

        Thread.sleep(100);

        model.setMessageToSend("Hello World");

        CountDownLatch latch = new CountDownLatch(1);
        model.sendMessageAsync(success -> latch.countDown());

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).as("Timed out waiting for server communication").isTrue();
        verify(postRequestedFor(urlEqualTo("/mytopic"))
                .withRequestBody(matching("Hello World")));
    }
}