module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.base;
    requires java.net.http;
    requires jdk.hotspot.agent;
    requires org.json;

    opens com.example to javafx.fxml;
    exports com.example;
}