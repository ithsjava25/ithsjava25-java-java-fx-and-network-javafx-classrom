module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;

    opens com.example to javafx.fxml;
    exports com.example;
}