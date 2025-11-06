module HelloFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires jdk.httpserver;

    opens com.example to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.example;
}
