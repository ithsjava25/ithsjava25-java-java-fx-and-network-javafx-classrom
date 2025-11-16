package com.example;

/**
 * Immutable chat message value object.
 *
 * Fields:
 * - id: optional ntfy message id (may be null)
 * - username: sender (never null; use "Anonymous" if unknown)
 * - message: text payload (may be empty)
 * - timestamp: formatted timestamp string (never null)
 * - fileName: optional attached file name (may be null)
 * - fileData: optional Base64 file data (may be null)
 */
public class ChatMessage {
    private final String id;
    private final String username;
    private final String message;
    private final String timestamp;
    private final String fileName;
    private final String fileData;

    /**
     * Full constructor.
     */
    public ChatMessage(String id, String username, String message, String timestamp,
                       String fileName, String fileData) {
        this.id = id;
        this.username = username == null ? "Anonymous" : username;
        this.message = message == null ? "" : message;
        this.timestamp = timestamp == null ? "" : timestamp;
        this.fileName = fileName;
        this.fileData = fileData;
    }

    /**
     * Constructor for file messages (no fileData).
     */
    public ChatMessage(String id, String username, String message, String timestamp, String fileName) {
        this(id, username, message, timestamp, fileName, null);
    }

    /**
     * Constructor for normal messages (no file).
     */
    public ChatMessage(String id, String username, String message, String timestamp) {
        this(id, username, message, timestamp, null, null);
    }

    /**
     * Convenience constructor when ID is not known.
     */
    public ChatMessage(String username, String message, String timestamp) {
        this(null, username, message, timestamp, null, null);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileData() {
        return fileData;
    }

    private String fileUrl;
    private String mimeType;

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String url) { this.fileUrl = url; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String t) { this.mimeType = t; }


    @Override
    public String toString() {
        if (fileName != null) {
            return username + " (" + timestamp + ")\n" + message + " [" + fileName + "]";
        } else {
            return username + " (" + timestamp + ")\n" + message;
        }
    }
}
