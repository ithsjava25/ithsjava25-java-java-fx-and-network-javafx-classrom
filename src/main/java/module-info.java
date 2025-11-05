module hellofx {
    requires javafx.controls;
    requires javafx.base;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.net.http;
    requires tools.jackson.databind;
    requires java.desktop;

    opens com.example to javafx.fxml;
    exports com.example;
}