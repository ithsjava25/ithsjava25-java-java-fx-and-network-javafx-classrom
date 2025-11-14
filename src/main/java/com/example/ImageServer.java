package com.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.nio.file.*;
import java.net.InetSocketAddress;
import java.util.UUID;

public class ImageServer {

    private final int port;
    private final Path imageDir;
    private HttpServer server; // ✅ Flytta HttpServer till instansfält

    public ImageServer(int port) throws IOException {
        this.port = port;
        this.imageDir = Paths.get("uploaded-images");
        if (!Files.exists(imageDir)) Files.createDirectory(imageDir);
        startServer();
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0); // ✅ Tilldela instansfältet

        // Hantera bilduppladdning
        server.createContext("/upload", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String filename = UUID.randomUUID() + ".jpg";
                Path filePath = imageDir.resolve(filename);
                Files.copy(exchange.getRequestBody(), filePath, StandardCopyOption.REPLACE_EXISTING);

                String imageUrl = "http://localhost:" + port + "/images/" + filename;
                byte[] response = imageUrl.getBytes();
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
            exchange.close();
        });

        // Visa uppladdade bilder
        server.createContext("/images", exchange -> {
            Path filePath = imageDir.resolve(exchange.getRequestURI().getPath().replace("/images/", ""));
            if (Files.exists(filePath)) {
                exchange.sendResponseHeaders(200, Files.size(filePath));
                Files.copy(filePath, exchange.getResponseBody());
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
            exchange.close();
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
