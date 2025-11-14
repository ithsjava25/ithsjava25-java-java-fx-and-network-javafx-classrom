package com.example;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class HelloController {

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextField inputField;
    @FXML private Label messageLabel;

    private HelloModel model = new HelloModel(new NtfyConnectionImpl());
    private Stage primaryStage;
    private File selectedFile;

    public void setPrimaryStage(Stage stage) { this.primaryStage = stage; }

    @FXML
    private void initialize() {
        messageView.setItems(model.getMessages());

        messageView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox hbox = new HBox(5);
                Label time = new Label("[" + item.getFormattedTime() + "] ");
                Label text = new Label(item.hasAttachment() ? item.getAttachmentName() : item.message());

                hbox.getChildren().add(time);

                // Visa bild
                if (item.hasAttachment() && item.getAttachmentContentType() != null &&
                        item.getAttachmentContentType().startsWith("image/")) {
                    Image img = model.loadImageFromUrl(item);
                    if (img != null) {
                        ImageView iv = new ImageView(img);
                        iv.setPreserveRatio(true);
                        iv.setFitWidth(300);
                        iv.setFitHeight(300);

                        iv.setOnMouseClicked(e -> {
                            if (e.getButton() == MouseButton.PRIMARY) openImageWindow(img);
                        });
                        hbox.getChildren().add(iv);
                    }
                }
                // Fil-ikoner
                else if (item.hasAttachment()) {
                    ImageView iv = new ImageView(new Image(
                            getClass().getResourceAsStream("/icons/file.png")));
                    iv.setFitWidth(24);
                    iv.setFitHeight(24);

                    iv.setOnMouseClicked(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            FileChooser chooser = new FileChooser();
                            chooser.setInitialFileName(item.getAttachmentName());
                            File dest = chooser.showSaveDialog(primaryStage);
                            if (dest != null) model.downloadAttachment(item, dest);
                        }
                    });
                    hbox.getChildren().addAll(iv, text);
                }
                // Textmeddelanden
                else {
                    hbox.getChildren().add(text);
                }

                setGraphic(hbox);
            }
        });
    }

    @FXML
    private void onSend() { sendMessage(); }

    @FXML
    private void sendMessage() {
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
        }
    }

    @FXML
    private void attachFile() {
        FileChooser chooser = new FileChooser();
        selectedFile = chooser.showOpenDialog(primaryStage);
        if (selectedFile != null) messageLabel.setText("Vald fil: " + selectedFile.getName());
    }

    private void openImageWindow(Image img) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(600);
        iv.setFitHeight(600);

        Button saveBtn = new Button("Spara bild");
        saveBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("image.png");
            File dest = chooser.showSaveDialog(stage);
            if (dest != null) {
                try {
                    javax.imageio.ImageIO.write(SwingFXUtils.fromFXImage(img, null),
                            "png", dest);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        VBox vbox = new VBox(5, iv, saveBtn);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.setTitle("Bildvisning");
        stage.showAndWait();
    }
}
