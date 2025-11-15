package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentTest {

    @Test
    @DisplayName("Attachment should store all properties correctly")
    void attachment_ShouldStoreAllProperties() {
        // Arrange & Act
        Attachment attachment = new Attachment("file.txt", "http://example.com/file", "text/plain", 1024L);

        // Assert
        assertThat(attachment.name()).isEqualTo("file.txt");
        assertThat(attachment.url()).isEqualTo("http://example.com/file");
        assertThat(attachment.type()).isEqualTo("text/plain");
        assertThat(attachment.size()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("Attachment equals and hashCode should work correctly")
    void attachment_EqualsAndHashCode_ShouldWork() {
        // Arrange
        Attachment attachment1 = new Attachment("file.txt", "http://example.com/file", "text/plain", 1024L);
        Attachment attachment2 = new Attachment("file.txt", "http://example.com/file", "text/plain", 1024L);
        Attachment attachment3 = new Attachment("different.txt", "http://example.com/different", "text/plain", 1024L);

        // Act & Assert
        assertThat(attachment1).isEqualTo(attachment2);
        assertThat(attachment1).isNotEqualTo(attachment3);
        assertThat(attachment1.hashCode()).isEqualTo(attachment2.hashCode());
    }
}