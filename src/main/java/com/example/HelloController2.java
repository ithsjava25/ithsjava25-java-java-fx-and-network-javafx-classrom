package com.example;

import javafx.fxml.FXML;

import javafx.scene.control.Label;


public class HelloController2 {

    @FXML
    private Label nameLabel;

    public void displayName(String userName) {
        nameLabel.setText("Hello: " + userName);
    }
}
