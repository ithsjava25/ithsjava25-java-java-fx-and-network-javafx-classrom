module hellofx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.chat to javafx.fxml;
    exports com.example.chat;
    
    opens com.example to javafx.graphics, javafx.fxml;
    exports com.example;
}
