package com.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.UUID;

public class ImageServer {

    private final int port;
    private final String baseUrl;
    private final Path imageDir;
    private HttpServer server;

    public ImageServer(int port, Path imageDir) throws IOException {
        this.port = port;
        this.imageDir = imageDir;
        if (!Files.exists(imageDir)) Files.createDirectories(imageDir);

        this.baseUrl = "http://localhost:" + port;

        startServer();
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Upload endpoint
        server.createContext("/upload", exchange -> {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                    return;
                }

                // Läs Content-Type från header
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                String extension = getExtensionForType(contentType);

                if (extension == null) {
                    exchange.sendResponseHeaders(415, -1); // Unsupported Media Type
                    return;
                }

                byte[] content = exchange.getRequestBody().readAllBytes();
                String filename = UUID.randomUUID() + extension;
                Path filePath = imageDir.resolve(filename);

                Files.write(filePath, content, StandardOpenOption.CREATE);

                // Returnera URL
                String imageUrl = baseUrl + "/images/" + filename;
                byte[] response = imageUrl.getBytes();
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } finally {
                exchange.close();
            }
        });

        // Serve images
        server.createContext("/images", exchange -> {
            try {
                Path filePath = imageDir.resolve(exchange.getRequestURI().getPath().replace("/images/", "")).normalize();
                if (Files.exists(filePath)) {
                    exchange.sendResponseHeaders(200, Files.size(filePath));
                    try (OutputStream os = exchange.getResponseBody()) {
                        Files.copy(filePath, os);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } finally {
                exchange.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("🖼️ Image server running at " + baseUrl);
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    private String getExtensionForType(String contentType) {
        if (contentType == null) return null;
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            default -> null;
        };
    }
}
