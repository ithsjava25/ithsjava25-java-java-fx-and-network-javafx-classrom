module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires io.github.cdimascio.dotenv.java;
    requires tools.jackson.databind;
    requires java.net.http;

    opens com.example to javafx.fxml;
    exports com.example;
}