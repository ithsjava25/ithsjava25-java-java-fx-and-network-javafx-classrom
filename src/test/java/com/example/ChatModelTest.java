package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatModelTest {

    private HttpClient mockHttpClient;
    private ChatModel chatModel;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
        chatModel = new ChatModel(mockHttpClient, Runnable::run);
    }

    @Test
    void testParseMessageReturnsMessage() {
        String data = "{\"event\":\"message\",\"message\":\"Hello World\"}";
        String result = chatModel.parseMessage(data);
        assertEquals("Hello World", result);
    }

    @Test
    void testParseMessageIgnoresNonMessageEvent() {
        String data = "{\"event\":\"update\",\"message\":\"Hello World\"}";
        String result = chatModel.parseMessage(data);
        assertNull(result);
    }

    @Test
    void testSendMessageCallsHttpClient() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);

        CompletableFuture<HttpResponse<String>> future = CompletableFuture.completedFuture(mockResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(future);

        chatModel.sendMessage("Hi there");

        Thread.sleep(100);

        verify(mockHttpClient, times(1))
                .sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testSubscribeCallsConsumer() throws Exception {
        String sseData = "data:{\"event\":\"message\",\"message\":\"Hello\"}\n";
        HttpResponse<java.io.InputStream> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn(new java.io.ByteArrayInputStream(sseData.getBytes()));

        CompletableFuture<HttpResponse<java.io.InputStream>> future = CompletableFuture.completedFuture(mockResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(future);

        AtomicReference<String> received = new AtomicReference<>();
        chatModel.subscribe(received::set);

        Thread.sleep(100);

        assertEquals("Hello", received.get());
    }
}
