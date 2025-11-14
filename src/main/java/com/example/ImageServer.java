package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.UUID;

public class ImageServer {

    private final int port;
    private final Path imageDir;
    private HttpServer server;

    public ImageServer(int port) throws IOException {
        this.port = port;
        this.imageDir = Paths.get("uploaded-images").toAbsolutePath().normalize();

        if (!Files.exists(imageDir)) {
            Files.createDirectories(imageDir);
        }

        startServer();
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // -------------------------------------------------------
        //                  UPLOAD HANDLER
        // -------------------------------------------------------
        server.createContext("/upload", exchange -> {
            try {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String filename = UUID.randomUUID() + ".jpg";
                Path filePath = imageDir.resolve(filename).normalize();

                // Extra säkerhet: kontrollera att filen ligger inne i imageDir
                if (!filePath.startsWith(imageDir)) {
                    exchange.sendResponseHeaders(403, -1);
                    return;
                }

                try {
                    Files.copy(exchange.getRequestBody(), filePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("❌ Failed to save uploaded file: " + e.getMessage());
                    exchange.sendResponseHeaders(500, -1);
                    return;
                }

                String imageUrl = "http://localhost:" + port + "/images/" + filename;
                byte[] response = imageUrl.getBytes();

                try (OutputStream os = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(200, response.length);
                    os.write(response);
                }

            } finally {
                exchange.close();
            }
        });

        // -------------------------------------------------------
        //             IMAGE FETCH HANDLER  /images/xxx
        // -------------------------------------------------------
        server.createContext("/images", exchange -> {
            try {
                String rawPath = exchange.getRequestURI().getPath().replace("/images/", "");
                if (rawPath.isBlank()) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                Path requested = imageDir.resolve(rawPath).normalize();

                if (!requested.startsWith(imageDir)) {
                    exchange.sendResponseHeaders(403, -1);
                    return;
                }

                if (!Files.exists(requested)) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }

                String contentType = Files.probeContentType(requested);
                if (contentType == null) contentType = "application/octet-stream";
                exchange.getResponseHeaders().add("Content-Type", contentType);

                long size = Files.size(requested);

                exchange.sendResponseHeaders(200, size);
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(requested, os);
                }

            } finally {
                exchange.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("🖼️ Image server running at http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Image server stopped");
        }
    }
}
