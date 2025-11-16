package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class HelloFX extends Application {

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

        String baseUrl = "https://ntfy.sh";
        String topic = "a5ce42c9-6d30-4f7b-942b-7e11d3075925";

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
                        System.out.println("Received raw line: " + line);
                    });
                });

        System.out.println("Listening for messages on topic: " + topic);
        Thread.sleep(Long.MAX_VALUE);
        launch();

    }

}