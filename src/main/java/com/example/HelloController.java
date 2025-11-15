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
import java.util.HashSet;
import java.util.Set;

public class HelloController {

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextField inputField;
    @FXML private Label messageLabel;

    private final HelloModel model = new HelloModel();
    private Stage primaryStage;
    private File selectedFile;
    private final String myTopic = "MY_TOPIC";
    private final Set<String> sentMessageIds = new HashSet<>(); // Track sent messages

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        try {
            messageView.setItems(model.getMessages());

            // Show welcome message
            showWelcomeMessage();

            messageView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(NtfyMessageDto item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    // Debug: Print message details to understand what's happening
                    System.out.println("Message ID: " + item.id() +
                            " | Topic: " + item.topic() +
                            " | My Topic: " + myTopic +
                            " | Message: " + item.message());

                    // Determine if this is our message or incoming
                    // Method 1: Check if we sent this message recently
                    boolean isMyMessage = sentMessageIds.contains(item.id());

                    // Method 2: If not found in sent messages, use topic comparison as fallback
                    if (!isMyMessage) {
                        isMyMessage = myTopic.equals(item.topic());
                    }

                    // Create the message content with sender label and timestamp
                    VBox messageContainer = new VBox(2);
                    messageContainer.setAlignment(Pos.CENTER);
                    messageContainer.setPadding(new Insets(5, 10, 5, 10));

                    // Sender label (You: for your messages, Incoming: for others)
                    Label senderLabel = new Label(isMyMessage ? "You:" : "Incoming:");
                    senderLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #00FF41; -fx-font-weight: bold;");

                    // Message content
                    HBox contentBox = new HBox(5);
                    contentBox.setAlignment(Pos.CENTER);

                    // Handle attachment icon
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
                        // Text message
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

                    // Timestamp
                    Label timeLabel = new Label(item.getFormattedTime());
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #00AA00;");

                    messageContainer.getChildren().addAll(senderLabel, contentBox, timeLabel);

                    setGraphic(messageContainer);
                    setText(null);
                    setAlignment(Pos.CENTER); // Center all messages
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

    @FXML
    private void onSend() { sendMessage(); }

    @FXML
    private void sendMessage() {
        try {
            if (selectedFile != null) {
                boolean ok = model.sendFile(selectedFile);
                messageLabel.setText(ok ? "File sent" : "File error");

                // When we send a file, we can't track the ID, so we'll rely on topic comparison
                // For files, we'll assume they show as "You:" based on topic
                selectedFile = null;
                return;
            }

            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                // Store that we're sending a message
                // Note: We can't get the message ID before sending, so we'll track it differently
                // For now, we'll rely on the topic comparison
                model.sendMessage(text);
                inputField.clear();
                messageLabel.setText("Message sent");

                // Add a small delay and then check the last message to mark it as ours
                Platform.runLater(() -> {
                    if (!model.getMessages().isEmpty()) {
                        NtfyMessageDto lastMessage = model.getMessages().get(model.getMessages().size() - 1);
                        if (lastMessage.message() != null && lastMessage.message().equals(text)) {
                            sentMessageIds.add(lastMessage.id());
                            messageView.refresh(); // Refresh to update the display
                        }
                    }
                });
            } else {
                messageLabel.setText("Write something before sending.");
            }
        } catch (Exception e) {
            messageLabel.setText("Send error: " + e.getMessage());
            System.err.println("❌ SEND ERROR: ");
            e.printStackTrace();
        }
    }

    private void showWelcomeMessage() {
        Platform.runLater(() -> {
            Alert welcomeAlert = new Alert(Alert.AlertType.INFORMATION);
            welcomeAlert.setTitle("Matrix Binary Chat");
            welcomeAlert.setHeaderText(null); // This completely removes the header

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

            // Auto-close using Timeline (JavaFX way) - 12 seconds
            Timeline autoCloseTimeline = new Timeline(
                    new KeyFrame(javafx.util.Duration.seconds(12), e -> welcomeAlert.close())
            );
            autoCloseTimeline.play();

            welcomeAlert.show();
        });
    }

    private ImageView createIconForAttachment(NtfyMessageDto item) {
        ImageView iconView = new ImageView();
        try {
            String type = item.getAttachmentContentType();
            File file = new File("downloads", item.getAttachmentName());

            if (type != null && type.startsWith("image/")) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/image.png")));

                if (file.exists()) {
                    // Fix: Use setOnMouseClicked instead of adding multiple handlers
                    iconView.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) { // Double click
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