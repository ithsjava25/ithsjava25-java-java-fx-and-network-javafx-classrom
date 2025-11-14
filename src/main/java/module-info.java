module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.net.http;
    requires javafx.graphics;
    requires java.desktop;

    requires com.fasterxml.jackson.databind;
    requires javafx.swing;

    opens com.example to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.example;
}