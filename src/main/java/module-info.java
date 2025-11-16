module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.net.http;
    requires java.sql;
    requires tools.jackson.databind;
    requires java.desktop;
    requires javafx.graphics;
    requires java.compiler;

    opens com.example to javafx.fxml;
    exports com.example;
}