package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    public ListView<NtfyMessageDto> messageView;

    @FXML
    public ImageView ntfyIcon;

    @FXML
    private TextField inputField;

    @FXML
    private Label messageLabel;

    private File selectedFile;
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void showRawMessageData() {
        NtfyMessageDto selected = messageView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TextArea textArea = new TextArea(selected.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            Stage stage = new Stage();
            stage.setTitle("Rå Meddelandedata - " + selected.getFormattedDateTime());
            stage.setScene(new Scene(scrollPane, 600, 400));
            stage.show();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Inget meddelande valt");
            alert.setHeaderText(null);
            alert.setContentText("Välj ett meddelande i listan för att se rådata.");
            alert.showAndWait();
        }
    }


    // Uppdatera även initialize för att lägga till context menu
    @FXML
    private void initialize() {
        try {
            ntfyIcon.setImage(new Image(getClass().getResourceAsStream("/Ntfy-Icon.png")));
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }

        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }

        messageView.setItems(model.getMessages());

        messageView.setCellFactory(lv -> new ListCell<NtfyMessageDto>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.hasAttachment()) {
                        setGraphic(createAttachmentCell(item));
                        setText(null);
                    } else {
                        setGraphic(null);
                        setText(item.getDisplayText());
                    }
                }
            }
        });

        // Lägg till context menu för att visa rådata
        ContextMenu contextMenu = new ContextMenu();
        MenuItem showRawData = new MenuItem("Visa rådata");
        showRawData.setOnAction(e -> showRawMessageData());
        contextMenu.getItems().add(showRawData);

        messageView.setContextMenu(contextMenu);

        // Dubbelklick för att visa rådata
        messageView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showRawMessageData();
            }
        });
    }


    private HBox createAttachmentCell(NtfyMessageDto item) {
        HBox hbox = new HBox(10);

        // Ikon baserat på filtyp
        ImageView fileIcon = new ImageView();
        try {
            String iconPath = getFileIconPath(item.getAttachmentContentType());
            fileIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            // Fallback ikon
            try {
                fileIcon.setImage(new Image(getClass().getResourceAsStream("/file-icon.png")));
            } catch (Exception ex) {
                // Ingen ikon
            }
        }
        fileIcon.setFitWidth(16);
        fileIcon.setFitHeight(16);

        // Add timestamp to attachment display
        String timeLabel = "[" + item.getFormattedTime() + "]";
        Label timeLabelControl = new Label(timeLabel);
        timeLabelControl.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");

        Label fileNameLabel = new Label(item.getAttachmentName());

        Button downloadBtn = new Button("Ladda ner");
        downloadBtn.setOnAction(e -> downloadAttachment(item));

        // För bilder: visa förhandsvisning
        if (item.getAttachmentContentType() != null &&
                item.getAttachmentContentType().startsWith("image/")) {
            Button previewBtn = new Button("Visa");
            previewBtn.setOnAction(e -> previewImage(item));
            hbox.getChildren().addAll(timeLabelControl, fileIcon, fileNameLabel, downloadBtn, previewBtn);
        } else {
            hbox.getChildren().addAll(timeLabelControl, fileIcon, fileNameLabel, downloadBtn);
        }

        return hbox;
    }

    private String getFileIconPath(String contentType) {
        if (contentType == null) return "/file-icon.png";

        if (contentType.startsWith("image/")) return "/image-icon.png";
        if (contentType.startsWith("video/")) return "/video-icon.png";
        if (contentType.startsWith("audio/")) return "/audio-icon.png";
        if (contentType.equals("application/pdf")) return "/pdf-icon.png";
        if (contentType.contains("word")) return "/word-icon.png";
        if (contentType.contains("excel")) return "/excel-icon.png";

        return "/file-icon.png";
    }

    private void downloadAttachment(NtfyMessageDto item) {
        if (item.hasAttachment()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Spara fil");
            fileChooser.setInitialFileName(item.getAttachmentName());

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                model.downloadAttachment(item, file);
            }
        }
    }

    private void previewImage(NtfyMessageDto item) {
        if (item.hasAttachment() && item.getAttachmentContentType().startsWith("image/")) {
            model.previewImage(item);
        }
    }

    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil att bifoga");

        Stage stage = primaryStage != null ? primaryStage : (Stage) messageLabel.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            messageLabel.setText("Fil vald: " + selectedFile.getName());
        } else {
            messageLabel.setText("Ingen fil vald.");
        }
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        if (selectedFile != null) {
            boolean success = model.sendFile(selectedFile);
            if (success) {
                messageLabel.setText("Filen '" + selectedFile.getName() + "' skickad!");
                selectedFile = null;
            } else {
                messageLabel.setText("Fel vid sändning av fil.");
            }
            return;
        }

        String enteredText = inputField.getText().trim();
        if (enteredText.isEmpty()) {
            messageLabel.setText("Vänligen skriv ett meddelande.");
            return;
        }

        model.sendMessage(enteredText);
        inputField.clear();
        messageLabel.setText("Meddelande skickat!");
    }

    @FXML
    private void sendFile(ActionEvent event) {
        handleAttachFile();
    }

    @FXML
    private void sendFileFromUrl(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Skicka fil från URL");
        dialog.setHeaderText("Ange URL till filen");
        dialog.setContentText("URL:");

        dialog.showAndWait().ifPresent(url -> {
            boolean success = model.sendFileFromUrl(url);
            if (success) {
                messageLabel.setText("Fil från URL skickad!");
            } else {
                messageLabel.setText("Fel vid sändning av fil från URL.");
            }
        });
    }
}