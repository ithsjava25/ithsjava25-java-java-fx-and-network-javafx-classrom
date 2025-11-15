package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NtfyMessageDtoTest {

    @Test
    @DisplayName("hasAttachment should return true when attachment exists")
    void hasAttachment_ShouldReturnTrue_WhenAttachmentExists() {
        // Arrange
        Attachment attachment = new Attachment("test.txt", "http://example.com/file", "text/plain", 100L);
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, attachment);

        // Act & Assert
        assertThat(message.hasAttachment()).isTrue();
    }

    @Test
    @DisplayName("hasAttachment should return false when attachment is null")
    void hasAttachment_ShouldReturnFalse_WhenAttachmentIsNull() {
        // Arrange
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, null);

        // Act & Assert
        assertThat(message.hasAttachment()).isFalse();
    }

    @Test
    @DisplayName("getAttachment methods should return correct values")
    void getAttachmentMethods_ShouldReturnCorrectValues() {
        // Arrange
        Attachment attachment = new Attachment("test.txt", "http://example.com/file", "text/plain", 100L);
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, attachment);

        // Act & Assert
        assertThat(message.getAttachmentName()).isEqualTo("test.txt");
        assertThat(message.getAttachmentUrl()).isEqualTo("http://example.com/file");
        assertThat(message.getAttachmentContentType()).isEqualTo("text/plain");
    }

    @Test
    @DisplayName("getAttachment methods should return null when no attachment")
    void getAttachmentMethods_ShouldReturnNull_WhenNoAttachment() {
        // Arrange
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, null);

        // Act & Assert
        assertThat(message.getAttachmentName()).isNull();
        assertThat(message.getAttachmentUrl()).isNull();
        assertThat(message.getAttachmentContentType()).isNull();
    }

    @Test
    @DisplayName("equals and hashCode should work correctly")
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Arrange
        NtfyMessageDto message1 = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, null);
        NtfyMessageDto message2 = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, null);
        NtfyMessageDto message3 = new NtfyMessageDto("2", 1234567890L, "message", "topic", "different", null, null);

        // Act & Assert
        assertThat(message1).isEqualTo(message2);
        assertThat(message1).isNotEqualTo(message3);
        assertThat(message1.hashCode()).isEqualTo(message2.hashCode());
    }

    @Test
    @DisplayName("toString should include all fields")
    void toString_ShouldIncludeAllFields() {
        // Arrange
        Attachment attachment = new Attachment("file.txt", "http://example.com/file", "text/plain", 1024L);
        NtfyMessageDto message = new NtfyMessageDto("123", 1763200650L, "message", "mytopic", "hello", "Title", attachment);

        // Act
        String result = message.toString();

        // Assert
        assertThat(result).contains("id='123'");
        assertThat(result).contains("time=1763200650");
        assertThat(result).contains("event='message'");
        assertThat(result).contains("topic='mytopic'");
        assertThat(result).contains("message='hello'");
        assertThat(result).contains("title='Title'");
        assertThat(result).contains("attachment=");
    }

    @Test
    @DisplayName("should handle message with all null fields in toString")
    void shouldHandleMessageWithAllNullFields_InToString() {
        // Arrange
        NtfyMessageDto message = new NtfyMessageDto(null, 0L, null, null, null, null, null);

        // Act
        String result = message.toString();

        // Assert - Should not throw exception
        assertThat(result).isNotNull();
    }
}