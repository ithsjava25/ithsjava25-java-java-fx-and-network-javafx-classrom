package com.example;

public record AttachmentDto(
        String name,
        String url,
        String type,
        long size) {}
