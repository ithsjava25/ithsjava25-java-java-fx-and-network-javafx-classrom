package com.example;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessage(String id, long time, String event, String topic, String message, HashMap<String, String> attachment){}
