module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires java.net.http;
    requires io.github.cdimascio.dotenv.java;
    requires com.fasterxml.jackson.databind;
    requires org.jetbrains.annotations;

    opens com.example to javafx.fxml;
    exports com.example;
}