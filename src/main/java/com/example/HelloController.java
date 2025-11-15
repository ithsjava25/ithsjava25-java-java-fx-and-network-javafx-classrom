package com.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HelloController {

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextField inputField;
    @FXML private Label messageLabel;

    private final HelloModel model = new HelloModel();
    private Stage primaryStage;
    private File selectedFile;
    private final String myTopic = "MY_TOPIC";
    private final Set<String> sentMessageIds = ConcurrentHashMap.newKeySet();
    private final Set<String> pendingMessageTexts = ConcurrentHashMap.newKeySet();
    private final Set<String> pendingFileNames = ConcurrentHashMap.newKeySet();

    /**
     * Sets the primary stage for this controller
     * @param stage the primary JavaFX stage to use for dialog windows
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        try {
            messageView.setItems(model.getMessages());
            inputField.setOnAction(event -> sendMessage());
            model.getMessages().addListener((javafx.collections.ListChangeListener.Change<? extends NtfyMessageDto> change) -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        Platform.runLater(() -> {
                            messageView.scrollTo(messageView.getItems().size() - 1);
                            messageView.refresh();
                        });
                    }
                }
            });

            showWelcomeMessage();

            messageView.setCellFactory(lv -> new ListCell<>() {
                /**
                 * Updates the list cell content for each message
                 * @param item the NtfyMessageDto to display, contains message data
                 * @param empty indicates if the cell is empty and should be cleared
                 */
                @Override
                protected void updateItem(NtfyMessageDto item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    boolean isMyMessage = isMyMessage(item);

                    VBox messageContainer = new VBox(2);
                    messageContainer.setAlignment(Pos.CENTER);
                    messageContainer.setPadding(new Insets(5, 10, 5, 10));

                    Label senderLabel = new Label(isMyMessage ? "You:" : "Incoming:");
                    senderLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #00FF41; -fx-font-weight: bold;");

                    HBox contentBox = new HBox(5);
                    contentBox.setAlignment(Pos.CENTER);

                    if (item.hasAttachment()) {
                        ImageView icon = createIconForAttachment(item);
                        icon.setFitWidth(20);
                        icon.setFitHeight(20);

                        Label messageLabel = new Label("📎 " + item.getAttachmentName());
                        messageLabel.setWrapText(true);
                        messageLabel.setMaxWidth(400);
                        messageLabel.setStyle(
                                "-fx-background-color: " + (isMyMessage ? "#003B00" : "#002800") + ";" +
                                        "-fx-text-fill: #00FF41;" +
                                        "-fx-padding: 8px 12px;" +
                                        "-fx-background-radius: 15px;" +
                                        "-fx-font-size: 14px;" +
                                        "-fx-border-color: " + (isMyMessage ? "#008F11" : "#006400") + ";" +
                                        "-fx-border-width: 1px;" +
                                        "-fx-border-radius: 15px;"
                        );

                        contentBox.getChildren().addAll(icon, messageLabel);
                    } else {
                        String displayText = getDisplayText(item);
                        Label messageLabel = new Label(displayText);
                        messageLabel.setWrapText(true);
                        messageLabel.setMaxWidth(400);
                        messageLabel.setStyle(
                                "-fx-background-color: " + (isMyMessage ? "#003B00" : "#002800") + ";" +
                                        "-fx-text-fill: #00FF41;" +
                                        "-fx-padding: 8px 12px;" +
                                        "-fx-background-radius: 15px;" +
                                        "-fx-font-size: 14px;" +
                                        "-fx-border-color: " + (isMyMessage ? "#008F11" : "#006400") + ";" +
                                        "-fx-border-width: 1px;" +
                                        "-fx-border-radius: 15px;"
                        );
                        contentBox.getChildren().add(messageLabel);
                    }

                    Label timeLabel = new Label(item.getFormattedTime());
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #00AA00;");

                    messageContainer.getChildren().addAll(senderLabel, contentBox, timeLabel);

                    setGraphic(messageContainer);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }

                private String getDisplayText(NtfyMessageDto item) {
                    if (item.message() != null && !item.message().isBlank()) {
                        return item.message();
                    } else {
                        return "(No message)";
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simplified method to determine if a message is from the current user
     * @param item the message DTO to check ownership of
     * @return true if the message was sent by current user, false otherwise
     */
    private boolean isMyMessage(NtfyMessageDto item) {
        if (sentMessageIds.contains(item.id())) {return true;}
        if (item.message() != null && pendingMessageTexts.contains(item.message())) {return true;}
        if (item.hasAttachment() && item.getAttachmentName() != null && pendingFileNames.contains(item.getAttachmentName())) {return true;}
        return myTopic.equals(item.topic());}

    @FXML
    private void onSend() { sendMessage(); }

    @FXML
    private void sendMessage() {
        try {
            if (selectedFile != null) {
                String fileName = selectedFile.getName();
                pendingFileNames.add(fileName);
                Platform.runLater(() -> messageView.refresh());

                boolean ok = model.sendFile(selectedFile);
                messageLabel.setText(ok ? "File sent" : "File error");
                Timeline cleanupTimeline = new Timeline(
                        new KeyFrame(javafx.util.Duration.millis(2000), e -> {
                            pendingFileNames.remove(fileName);
                            trackRealFileId(fileName);
                        })
                );
                cleanupTimeline.play();

                selectedFile = null;
                return;
            }

            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                pendingMessageTexts.add(text);
                Platform.runLater(() -> messageView.refresh());
                model.sendMessage(text);
                inputField.clear();
                messageLabel.setText("Message sent");
                Timeline cleanupTimeline = new Timeline(
                        new KeyFrame(javafx.util.Duration.millis(2000), e -> {
                            pendingMessageTexts.remove(text);
                            trackRealMessageId(text);
                        })
                );
                cleanupTimeline.play();

            } else {
                messageLabel.setText("Write something before sending.");
            }
        } catch (Exception e) {
            messageLabel.setText("Send error: " + e.getMessage());
            System.err.println("❌ SEND ERROR: ");
            e.printStackTrace();
        }
    }

    /**
     * Find the real message ID for a sent message by matching text content
     * @param expectedText the text content to search for in received messages
     */
    private void trackRealMessageId(String expectedText) {
        for (NtfyMessageDto message : model.getMessages()) {
            if (expectedText.equals(message.message()) &&
                    !sentMessageIds.contains(message.id())) {
                sentMessageIds.add(message.id());
                Platform.runLater(() -> messageView.refresh());
                break;
            }
        }
    }

    /**
     * Find the real message ID for a sent file
     */
    private void trackRealFileId(String expectedFileName) {
        for (NtfyMessageDto message : model.getMessages()) {
            if (message.hasAttachment() && expectedFileName.equals(message.getAttachmentName()) &&
                    !sentMessageIds.contains(message.id())) {
                sentMessageIds.add(message.id());
                Platform.runLater(() -> messageView.refresh());
                break;
            }
        }
    }

    private void showWelcomeMessage() {
        Platform.runLater(() -> {
            Alert welcomeAlert = new Alert(Alert.AlertType.INFORMATION);
            welcomeAlert.setTitle("Matrix Binary Chat");
            welcomeAlert.setHeaderText(null);

            String welcomeText = "Welcome to the Matrix Binary Chat!\n\n" +
                    "💡 Tips:\n" +
                    "• Double tap picture icon to open pictures\n" +
                    "• Files and pictures are automatically downloaded to 'downloads' folder\n" +
                    "• Chat in real-time with binary encryption\n\n" +
                    "This message will auto-close in 12 seconds...";

            Label contentLabel = new Label(welcomeText);
            contentLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-text-fill: #00FF41; -fx-background-color: #001100; -fx-padding: 10px;");
            contentLabel.setWrapText(true);

            welcomeAlert.getDialogPane().setContent(contentLabel);
            welcomeAlert.getDialogPane().setStyle(
                    "-fx-background-color: #001100; " +
                            "-fx-border-color: #00FF41; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 5px;"
            );
            welcomeAlert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                    "-fx-background-color: #003B00; " +
                            "-fx-text-fill: #00FF41; " +
                            "-fx-border-color: #00FF41; " +
                            "-fx-font-family: 'Consolas';"
            );
            welcomeAlert.setGraphic(null);
            Timeline autoCloseTimeline = new Timeline(
                    new KeyFrame(javafx.util.Duration.seconds(12), e -> welcomeAlert.close())
            );
            autoCloseTimeline.play();
            welcomeAlert.show();
        });
    }

    /**
     * Creates an appropriate icon view for different attachment types
     * @param item the message DTO containing attachment information
     * @return ImageView configured with appropriate icon and click handlers
     */
    private ImageView createIconForAttachment(NtfyMessageDto item) {
        ImageView iconView = new ImageView();
        try {
            String type = item.getAttachmentContentType();
            File file = new File("downloads", item.getAttachmentName());

            if (type != null && type.startsWith("image/")) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/image.png")));

                if (file.exists()) {
                    iconView.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            ImageView fullImage = new ImageView(new Image(file.toURI().toString()));
                            fullImage.setPreserveRatio(true);
                            fullImage.setFitWidth(600);
                            fullImage.setFitHeight(600);

                            Stage stage = new Stage();
                            stage.setTitle(item.getAttachmentName());
                            stage.setScene(new Scene(new StackPane(fullImage), 600, 600));
                            stage.show();
                        }
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
            if (selectedFile != null) messageLabel.setText("Selected file: " + selectedFile.getName());
        } catch (Exception e) {
            showAlert("File selection error: " + e.getMessage());
            System.err.println("❌ FILE CHOOSER ERROR: ");
            e.printStackTrace();
        }
    }

    /**
     * Shows an error alert dialog with the specified message
     * @param msg the error message to display in the alert dialog
     */
    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}