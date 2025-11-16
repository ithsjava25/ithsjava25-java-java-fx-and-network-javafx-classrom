package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML
    public ListView<NtfyMessageDto> messageView ;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField messageField;

    @FXML
    private Button emojiButton;

    @FXML
    private void emojis() {

        String[] emojis = {"😀", "😂", "😍", "😎", "😭", "👍", "🎉"};


        ContextMenu emojiMenu = new ContextMenu();
        for (String emoji : emojis) {
            MenuItem item = new MenuItem(emoji);
            item.setOnAction(e -> {
                messageField.appendText(emoji);
            });
            emojiMenu.getItems().add(item);
        }

        emojiMenu.show(emojiButton, Side.BOTTOM, 0, 0);
    }

    @FXML
    private void initialize() {
        messageLabel.setText(model.getGreeting());
        messageView.setItems(model.getMessages());
        messageView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setGraphic(null);
                    return;
                }

                Label label = new Label(msg.toString());
                label.setWrapText(true);
                label.setMaxWidth(180);


                boolean fromMe = msg.topic() != null && msg.topic().equals("me");

                if (fromMe) {
                    label.setStyle("-fx-background-color: lightgreen; -fx-padding: 6; -fx-background-radius: 8;");
                    setAlignment(Pos.CENTER_RIGHT);
                } else {
                    label.setStyle("-fx-background-color: lightgray; -fx-padding: 6; -fx-background-radius: 8;");
                    setAlignment(Pos.CENTER_LEFT);
                }

                setGraphic(label);
            }
        });

    }



    public void sendMessage(ActionEvent actionEvent) {
        String text = messageField.getText().trim();
        if (!text.isEmpty()) {
            model.sendMessage(text);
            messageField.clear();
        }
    }
}
