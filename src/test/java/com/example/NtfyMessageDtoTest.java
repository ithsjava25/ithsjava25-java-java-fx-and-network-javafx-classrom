package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NtfyMessageDto} class.
 */
class NtfyMessageDtoTest {

    @Test
    @DisplayName("hasAttachment should return true when attachment exists")
    void hasAttachment_ShouldReturnTrue_WhenAttachmentExists() {
        Attachment attachment = new Attachment("test.txt", "http://example.com/file", "text/plain", 100L);
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, attachment);

        assertThat(message.hasAttachment()).isTrue();
    }

    @Test
    @DisplayName("hasAttachment should return false when attachment is null")
    void hasAttachment_ShouldReturnFalse_WhenAttachmentIsNull() {
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, null);

        assertThat(message.hasAttachment()).isFalse();
    }

    @Test
    @DisplayName("getAttachment methods should return correct values")
    void getAttachmentMethods_ShouldReturnCorrectValues() {
        Attachment attachment = new Attachment("test.txt", "http://example.com/file", "text/plain", 100L);
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, attachment);

        assertThat(message.getAttachmentName()).isEqualTo("test.txt");
        assertThat(message.getAttachmentUrl()).isEqualTo("http://example.com/file");
        assertThat(message.getAttachmentContentType()).isEqualTo("text/plain");
    }

    @Test
    @DisplayName("getAttachment methods should return null when no attachment")
    void getAttachmentMethods_ShouldReturnNull_WhenNoAttachment() {
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, null);

        assertThat(message.getAttachmentName()).isNull();
        assertThat(message.getAttachmentUrl()).isNull();
        assertThat(message.getAttachmentContentType()).isNull();
    }

    @Test
    @DisplayName("toString should include all fields")
    void toString_ShouldIncludeAllFields() {
        Attachment attachment = new Attachment("file.txt", "http://example.com/file", "text/plain", 1024L);
        NtfyMessageDto message = new NtfyMessageDto("123", 1763200650L, "message", "mytopic", "hello", "Title", attachment);

        String result = message.toString();

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
        NtfyMessageDto message = new NtfyMessageDto(null, 0L, null, null, null, null, null);

        String result = message.toString();
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getFormattedTime should format timestamp correctly")
    void getFormattedTime_ShouldFormatTimestampCorrectly() {
        long timestamp = 1763200800L; // 2025-11-15 12:00:00 UTC
        NtfyMessageDto message = new NtfyMessageDto("1", timestamp, "message", "topic", "test", null, null);

        String formattedTime = message.getFormattedTime();

        assertThat(formattedTime).isNotNull();
        assertThat(formattedTime).matches("\\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("getFormattedTime should handle zero timestamp")
    void getFormattedTime_ShouldHandleZeroTimestamp() {
        NtfyMessageDto message = new NtfyMessageDto("1", 0L, "message", "topic", "test", null, null);

        String formattedTime = message.getFormattedTime();

        assertThat(formattedTime).isNotNull();
        assertThat(formattedTime).matches("\\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("getFormattedTime should handle negative timestamp")
    void getFormattedTime_ShouldHandleNegativeTimestamp() {
        NtfyMessageDto message = new NtfyMessageDto("1", -1L, "message", "topic", "test", null, null);

        String formattedTime = message.getFormattedTime();

        assertThat(formattedTime).isNotNull();
        assertThat(formattedTime).matches("\\d{2}:\\d{2}:\\d{2}");
    }
}
