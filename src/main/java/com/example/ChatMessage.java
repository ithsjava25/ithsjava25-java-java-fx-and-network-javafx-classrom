package com.example;

public class ChatMessage {
    private final String username;
    private final String message;
    private final String timestamp;
    private final String fileName;
    private final String fileData;


    public ChatMessage(String username, String message, String timestamp,
                       String fileName, String fileData) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.fileName = fileName;
        this.fileData = fileData; // always set
    }

    public ChatMessage(String username, String message, String timestamp,
                       String fileName) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.fileName = fileName;
        this.fileData = null;
    }

    public ChatMessage(String username, String message, String timestamp) {
        this(username, message, timestamp, null, null);
    }

    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getFileName() { return fileName; }
    public String getFileData() { return fileData; }
}
