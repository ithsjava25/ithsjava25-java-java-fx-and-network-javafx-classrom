package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(String id, long time, String event, String topic, String message){
    private static final DateTimeFormatter date =
            DateTimeFormatter.ofPattern("HH:mm");
    @Override
    public String toString(){
        String timeStr = new SimpleDateFormat("HH:mm").format(new Date(time * 1000));
        return timeStr+ "  " + message ;
    }


}
