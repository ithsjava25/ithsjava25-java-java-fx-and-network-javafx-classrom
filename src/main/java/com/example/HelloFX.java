package com.example;

import com.example.client.ChatNetworkClient;
import com.example.client.NtfyHttpClient;
import com.example.domain.ChatModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;


import static com.example.utils.EnvLoader.loadEnv;

public class HelloFX extends Application {
    private static final Logger log = LoggerFactory.getLogger("MAIN");
    static final ChatModel model = new ChatModel();

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloFX.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        HelloController controller = fxmlLoader.getController();
        controller.setModel(model);
        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("Hello MVC");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        Properties env = loadEnv();

        String baseUrl = env.getProperty("NTFY_BASE_URL");
        String topic = env.getProperty("NTFY_TOPIC");

        ChatNetworkClient client = new NtfyHttpClient(model);

        ChatNetworkClient.Subscription sub = client.subscribe(baseUrl, topic);
        log.info("Subscription: {}", sub);
        // client.send(baseUrl, topic, "I've been expecting you.");

        launch();

    }

}