module com.example.javafxchatapp {
    exports com.example;
    exports com.example.controller;
    exports com.example.model;
    exports com.example.network;
    exports com.example.util;

    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens com.example.controller to javafx.fxml;
    opens com.example to javafx.graphics;
    opens com.example.model to com.fasterxml.jackson.databind;
}