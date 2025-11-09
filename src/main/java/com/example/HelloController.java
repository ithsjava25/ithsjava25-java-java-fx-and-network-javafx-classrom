package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML
    private Label messageLabel;
    @FXML
    private Label currentDateAndTime;
    @FXML
    private Button updateButton;

    private Timeline timeline;
    private boolean updateButtonState = true;

    public HelloModel getModel() {
        return model;
    }

    @FXML
    ListView<String> listView;


    @FXML
    private void initialize() {

    }
}
