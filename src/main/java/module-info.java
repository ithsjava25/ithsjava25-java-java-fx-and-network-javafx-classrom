module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http; // tillagd

    opens com.example.chat to javafx.fxml;
    exports com.example.chat;
}

