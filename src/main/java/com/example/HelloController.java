package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class HelloController {

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextField inputField;
    @FXML private Label messageLabel;

    private final HelloModel model = new HelloModel();
    private Stage primaryStage;
    private File selectedFile;
    private final String myTopic = "MY_TOPIC";

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        try {
            messageView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
            messageView.setItems(model.getMessages());

            messageView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(NtfyMessageDto item, boolean empty) {
                    try {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        boolean isIncoming = !myTopic.equals(item.topic());
                        HBox cellBox = new HBox(5);
                        cellBox.setMaxWidth(Double.MAX_VALUE);
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        // Hantera text
                        String displayText;
                        if (item.hasAttachment()) {
                            displayText = item.getAttachmentName();
                        } else if (item.message() != null && !item.message().isBlank()) {
                            displayText = item.message();
                        } else {
                            displayText = null;
                        }

                        Label text = new Label();
                        if (displayText != null) {
                            text.setText(displayText);
                        } else if (isIncoming) {
                            text.setText("(Inget meddelande)");
                        } else {
                            text.setText(""); // tom text för skickade meddelanden
                        }

                        text.setWrapText(true);
                        text.setMaxWidth(400);
                        text.setStyle(
                                "-fx-background-color: " + (isIncoming ? "#DDDDDDCC" : "#4CAF50CC") + ";" +
                                        "-fx-text-fill: " + (isIncoming ? "black" : "white") + ";" +
                                        "-fx-padding: 8;" +
                                        "-fx-background-radius: 10;"
                        );

                        // Hantera ikon
                        ImageView iconView = null;
                        if (item.hasAttachment()) {
                            iconView = createIconForAttachment(item);
                        }

                        if (isIncoming) {
                            if (iconView != null) cellBox.getChildren().add(iconView);
                            cellBox.getChildren().add(text);
                        } else {
                            cellBox.getChildren().add(spacer);
                            cellBox.getChildren().add(text);
                            if (iconView != null) cellBox.getChildren().add(iconView);
                        }

                        HBox wrapper = new HBox(cellBox);
                        wrapper.setMaxWidth(Double.MAX_VALUE);
                        wrapper.setAlignment(isIncoming ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
                        setGraphic(wrapper);

                    } catch (Exception e) {
                        System.err.println("❌ Fel i cell-factory: " + e.getMessage());
                        setText("(Fel vid visning)");
                        setGraphic(null);
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Fel vid initialisering: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onSend() { sendMessage(); }

    @FXML
    private void sendMessage() {
        try {
            if (selectedFile != null) {
                boolean ok = model.sendFile(selectedFile);
                messageLabel.setText(ok ? "Filen skickad" : "Fel vid fil");
                selectedFile = null;
                return;
            }

            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                model.sendMessage(text);
                inputField.clear();
                messageLabel.setText("Meddelande skickat");
            } else {
                messageLabel.setText("Skriv något innan du skickar.");
            }
        } catch (Exception e) {
            messageLabel.setText("Fel vid sändning: " + e.getMessage());
            System.err.println("❌ SEND ERROR: ");
            e.printStackTrace();
        }
    }

    private ImageView createIconForAttachment(NtfyMessageDto item) {
        ImageView iconView = new ImageView();
        try {
            iconView.setFitWidth(24);
            iconView.setFitHeight(24);

            String type = item.getAttachmentContentType();
            File file = new File("downloads", item.getAttachmentName());

            if (type != null && type.startsWith("image/")) {
                // Alltid visa image.png som ikon
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/image.png")));

                // Klickbar bild för att öppna i nytt fönster
                if (file.exists()) {
                    iconView.setOnMouseClicked(e -> {
                        ImageView fullImage = new ImageView(new Image(file.toURI().toString()));
                        fullImage.setPreserveRatio(true);
                        fullImage.setFitWidth(600);
                        fullImage.setFitHeight(600);

                        Stage stage = new Stage();
                        stage.setTitle(item.getAttachmentName());
                        stage.setScene(new Scene(new StackPane(fullImage), 600, 600));
                        stage.show();
                    });
                }

            } else if ("application/pdf".equals(type)) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/pdf.png")));
            } else if ("application/zip".equals(type)) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/zip.png")));
            } else {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/file.png")));
            }

        } catch (Exception e) {
            System.err.println("❌ ICON ERROR: " + e.getMessage());
            e.printStackTrace();
            try {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/file.png")));
            } catch (Exception ignored) {}
        }

        return iconView;
    }

    @FXML
    private void attachFile() {
        try {
            FileChooser chooser = new FileChooser();
            selectedFile = chooser.showOpenDialog(primaryStage);
            if (selectedFile != null) messageLabel.setText("Vald fil: " + selectedFile.getName());
        } catch (Exception e) {
            showAlert("Fel vid filval: " + e.getMessage());
            System.err.println("❌ FILE CHOOSER ERROR: ");
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fel");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
