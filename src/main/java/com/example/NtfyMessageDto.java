package com.example;




public record NtfyMessageDto(String id, long time, String event, String topic, String message, boolean isLocal) {

    public NtfyMessageDto(String id, long time, String event, String topic, String message) {
        this(id, time, event, topic, message, false);
    }

    public NtfyMessageDto(String message) {
        this(null, 0, "message", null, message, true);
    }


    @Override
    public String toString(){
            return message;
    }

}

