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

            if (currentDateAndTime != null) {
                currentDateAndTime.textProperty().bind(model.dateTimeProperty());
            }

            if (messageLabel != null) {
                messageLabel.setText(model.getGreeting());
            }

            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                model.setDateTime(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyy-MM-dd hh:mm:ss")));
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

        }
        public void updateButtonAction(ActionEvent actionEvent) {
            if (updateButtonState) {
                timeline.stop();
            }
            else {
                timeline.play();
            }

            updateButton.setText(updateButtonState ? "Start" : "Stop");
            updateButtonState = !updateButtonState;
        }
    }
