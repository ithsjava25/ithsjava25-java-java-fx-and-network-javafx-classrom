package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        String hostFromEnv = dotenv.get("HOST_NAME");

        if (hostFromEnv == null || hostFromEnv.isBlank()) {
            throw new IllegalStateException("HOST_NAME saknas i .env-filen!");
        }

        hostName = hostFromEnv;
        System.out.println("HOST_NAME: " + hostName);
    }

    @Override
    public boolean send(String message) {
        if (message == null || message.isBlank()) {
            System.err.println("⚠️ Meddelandet är tomt, inget skickas.");
            return false;
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .header("Cache", "no")
                .build();

        CompletableFuture<HttpResponse<Void>> future = http.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding());

        future.exceptionally(ex -> {
            System.err.println("❌ Fel vid sändning: " + ex.getMessage());
            return null;
        });

        return true;
    }

    @Override
    public boolean sendFile(File file, String filename) {
        try {
            if (!file.exists()) {
                System.err.println("❌ Filen finns inte: " + file.getAbsolutePath());
                return false;
            }

            // Kontrollera filstorlek (max 15MB för ntfy.sh)
            long fileSize = file.length();
            if (fileSize > 15 * 1024 * 1024) {
                System.err.println("❌ Filen är för stor: " + fileSize + " bytes (max 15MB)");
                return false;
            }

            String actualFilename = (filename != null) ? filename : file.getName();

            // Bestäm Content-Type baserat på filändelse
            String contentType = getContentType(actualFilename);

            System.out.println("🖼️ Skickar bild: " + actualFilename);
            System.out.println("📏 Storlek: " + fileSize + " bytes");
            System.out.println("📁 Content-Type: " + contentType);

            // VIKTIGT: Använd PUT med korrekt Content-Type och Filename header
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Filename", actualFilename)
                    .header("Content-Type", contentType)
                    .header("Cache", "no")
                    .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .build();

            System.out.println("🔗 Skickar till: " + hostName + "/mytopic");

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Response status: " + response.statusCode());
            System.out.println("📡 Response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("✅ Bild skickad: " + actualFilename);

                // Kontrollera response
                if (response.body().contains("attachment")) {
                    System.out.println("🎉 Bilden laddades upp med bilaga!");
                }
                return true;
            } else {
                System.err.println("❌ Fel vid bildöverföring: " + response.statusCode() + " - " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Fel vid bildöverföring: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Hjälpmetod för att bestämma Content-Type
    private String getContentType(String filename) {
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream";
        }
    }

    @Override
    public boolean sendFileFromUrl(String fileUrl, String filename) {
        try {
            String actualFilename = (filename != null) ? filename : extractFilenameFromUrl(fileUrl);

            System.out.println("📤 Skickar fil från URL: " + fileUrl);
            System.out.println("📁 Filnamn: " + actualFilename);

            // Använd enklare approach med headers istället för JSON
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostName + "/mytopic"))
                    .header("Attach", fileUrl)
                    .header("Filename", actualFilename)
                    .header("Cache", "no")
                    .POST(HttpRequest.BodyPublishers.ofString("Fil: " + actualFilename))
                    .build();

            System.out.println("🔗 Skickar till: " + hostName + "/mytopic");

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Response status: " + response.statusCode());
            System.out.println("📡 Response body: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("✅ Fil från URL skickad: " + actualFilename);
                return true;
            } else {
                System.err.println("❌ Fel vid URL-filöverföring: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Fel vid URL-filöverföring: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String extractFilenameFromUrl(String url) {
        try {
            String filename = url.substring(url.lastIndexOf('/') + 1);
            if (filename.contains("?")) {
                filename = filename.substring(0, filename.indexOf("?"));
            }
            // Säkerställ att filen har rätt extension
            if (!filename.contains(".")) {
                filename += ".jpg"; // default till jpg om ingen extension
            }
            return filename;
        } catch (Exception e) {
            return "image.jpg";
        }
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .build();

        http.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    System.out.println("📥 Mottar meddelanden... Status: " + response.statusCode());
                    response.body().forEach(line -> {
                        try {
                            NtfyMessageDto msg = mapper.readValue(line, NtfyMessageDto.class);

                            if ("message".equals(msg.event())) {
                                String text = msg.message() != null ? msg.message() : "(inget meddelande)";
                                System.out.println("✅ Mottaget: " + text);

                                // Hantera bilagor
                                if (msg.attachment() != null) {
                                    NtfyMessageDto.Attachment att = msg.attachment();
                                    String attName = att.name() != null ? att.name() : "unknown";
                                    String attType = att.type() != null ? att.type() : "application/octet-stream";
                                    String attUrl = att.url();

                                    System.out.println("📎 Bilaga: " + attName + " (" + attType + ")");
                                    System.out.println("🔗 URL: " + attUrl);
                                }

                                // Skicka vidare till controller / ListView
                                messageHandler.accept(msg);
                            }

                        } catch (JsonProcessingException e) {
                            System.err.println("⚠️ JSON parsing error: " + e.getMessage());
                        }
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("❌ Network error while receiving messages: " + ex.getMessage());
                    return null;
                });
    }
}