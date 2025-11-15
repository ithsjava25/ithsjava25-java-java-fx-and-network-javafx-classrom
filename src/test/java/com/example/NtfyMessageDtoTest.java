package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NtfyMessageDto} class.
 * Tests attachment handling, getter methods, toString representation, and time formatting.
 */
class NtfyMessageDtoTest {

    /**
     * Tests that hasAttachment returns true when a valid attachment is present.
     */
    @Test
    @DisplayName("hasAttachment should return true when attachment exists")
    void hasAttachment_ShouldReturnTrue_WhenAttachmentExists() {
        // Arrange
        Attachment attachment = new Attachment("test.txt", "http://example.com/file", "text/plain", 100L);
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, attachment);

        // Act & Assert
        assertThat(message.hasAttachment()).isTrue();
    }

    /**
     * Tests that hasAttachment returns false when the attachment field is null.
     */
    @Test
    @DisplayName("hasAttachment should return false when attachment is null")
    void hasAttachment_ShouldReturnFalse_WhenAttachmentIsNull() {
        // Arrange
        NtfyMessageDto message = new NtfyMessageDto("1", 1234567890L, "message", "topic", "test", null, null);

        // Act & Assert
        assertThat(message.hasAttachment()).isFalse();
    }

    /**
     * Tests that all attachment getter methods return correct values when an attachment is present.
     */
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

    /**
     * Tests that all attachment getter methods return null when no attachment is present.
     */
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

    /**
     * Tests that the toString method includes all field values when they are present.
     */
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

    /**
     * Tests that the toString method handles null values gracefully without throwing exceptions.
     */
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

    /**
     * Tests that getFormattedTime formats a valid timestamp correctly.
     */
    @Test
    @DisplayName("getFormattedTime should format timestamp correctly")
    void getFormattedTime_ShouldFormatTimestampCorrectly() {
        // Arrange - Using a known timestamp (2025-11-15 12:00:00 UTC)
        long timestamp = 1763200800L; // This represents a specific date/time
        NtfyMessageDto message = new NtfyMessageDto("1", timestamp, "message", "topic", "test", null, null);

        // Act
        String formattedTime = message.getFormattedTime();

        // Assert - Should format the timestamp to HH:mm:ss pattern
        assertThat(formattedTime).isNotNull();
        assertThat(formattedTime).matches("\\d{2}:\\d{2}:\\d{2}"); // HH:mm:ss pattern
    }

    /**
     * Tests that getFormattedTime handles zero timestamp (epoch) correctly.
     */
    @Test
    @DisplayName("getFormattedTime should handle zero timestamp")
    void getFormattedTime_ShouldHandleZeroTimestamp() {
        // Arrange - Zero timestamp (epoch - January 1, 1970)
        NtfyMessageDto message = new NtfyMessageDto("1", 0L, "message", "topic", "test", null, null);

        // Act
        String formattedTime = message.getFormattedTime();

        // Assert - Should not crash and should return a valid time format
        assertThat(formattedTime).isNotNull();
        assertThat(formattedTime).matches("\\d{2}:\\d{2}:\\d{2}"); // HH:mm:ss pattern
    }

    /**
     * Tests that getFormattedTime handles negative timestamp (pre-1970) correctly.
     */
    @Test
    @DisplayName("getFormattedTime should handle negative timestamp")
    void getFormattedTime_ShouldHandleNegativeTimestamp() {
        // Arrange - Negative timestamp (December 31, 1969)
        NtfyMessageDto message = new NtfyMessageDto("1", -1L, "message", "topic", "test", null, null);

        // Act
        String formattedTime = message.getFormattedTime();

        // Assert - Should handle negative timestamps gracefully
        assertThat(formattedTime).isNotNull();
        assertThat(formattedTime).matches("\\d{2}:\\d{2}:\\d{2}"); // HH:mm:ss pattern
    }
}