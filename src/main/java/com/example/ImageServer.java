package com.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.nio.file.*;
import java.net.InetSocketAddress;
import java.util.UUID;

public class ImageServer {

    private final int port;
    private final Path imageDir;
    private HttpServer server;

    public ImageServer(int port) throws IOException {
        this.port = port;
        this.imageDir = Paths.get("uploaded-images");
        if (!Files.exists(imageDir)) Files.createDirectory(imageDir);
        startServer();
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Hantera bilduppladdning
        server.createContext("/upload", exchange -> {
            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    String filename = UUID.randomUUID() + ".jpg";
                    Path filePath = imageDir.resolve(filename);

                    try {
                        Files.copy(exchange.getRequestBody(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("❌ Failed to save uploaded file: " + e.getMessage());
                        exchange.sendResponseHeaders(500, -1);
                        return;
                    }

                    String imageUrl = "http://localhost:" + port + "/images/" + filename;
                    byte[] response = imageUrl.getBytes();
                    try {
                        exchange.sendResponseHeaders(200, response.length);
                        exchange.getResponseBody().write(response);
                    } catch (IOException e) {
                        System.err.println("❌ Failed to write response: " + e.getMessage());
                    }
                }
            } finally {
                exchange.close();
            }
        });

        // Visa uppladdade bilder
        server.createContext("/images", exchange -> {
            try {
                Path filePath = imageDir.resolve(exchange.getRequestURI().getPath().replace("/images/", ""));
                if (Files.exists(filePath)) {
                    exchange.sendResponseHeaders(200, Files.size(filePath));
                    try {
                        Files.copy(filePath, exchange.getResponseBody());
                    } catch (IOException e) {
                        System.err.println("❌ Failed to send image: " + e.getMessage());
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
        System.out.println("🖼️ Image server running at http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Image server stopped");
        }
    }
}
