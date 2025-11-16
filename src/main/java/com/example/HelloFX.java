package com.example;

import com.example.domain.NtfyMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.UUID;


import static com.example.utils.EnvLoader.loadEnv;

public class HelloFX extends Application {
    private static final Logger logger = LoggerFactory.getLogger("MAIN");

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("Hello MVC");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Properties env = loadEnv();

        String baseUrl = env.getProperty("NTFY_BASE_URL");
        String topic = env.getProperty("NTFY_TOPIC");
        ObjectMapper mapper = new ObjectMapper();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest sendRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + topic))
                .POST(HttpRequest.BodyPublishers.ofString("I've been expecting you."))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + topic + "/json"))
                .GET()
                .build();

        client.send(sendRequest, HttpResponse.BodyHandlers.discarding());

        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    response.body().forEach(line -> {
                        logger.info("Event received: {}", line);

                        try {
                            NtfyMessage msg = mapper.readValue(line, NtfyMessage.class);
                            logger.info("NtfyMessage:\n  id     = {}\n  event  = {}\n  topic  = {}\n  message= {}",
                                    msg.id(), msg.event(), msg.topic(), msg.message());
                        } catch (Exception e) {
                            logger.error("Failed to parse line: {}", line, e);
                        }
                    });
                });

        System.out.println("Listening for messages on topic: " + topic);
        Thread.sleep(Long.MAX_VALUE);
        launch();

    }

}