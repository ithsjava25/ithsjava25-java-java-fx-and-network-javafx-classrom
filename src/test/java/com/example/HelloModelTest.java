package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloModelTest {

    @Test
    @DisplayName("When calling sendMessage it should call connection send")
    void sendMessageCallsConnectionWithMessageToSend() {
        //Arrange
        var spy = new NtfyConnectionSpy();
        var model = new HelloModel(spy);

        //Act
        model.sendMessage("Hello World");

        //Assert
        assertThat(spy.message).isEqualTo("Hello World");
    }

}
