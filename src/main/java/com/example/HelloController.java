package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class HelloController implements Initializable {

    @FXML private ListView<ChatMessage> chatList;
    @FXML private TextField inputField;
    @FXML private TextField usernameField;
    @FXML private CheckBox hideMyMessagesCheck;

    private final HelloModel model = new HelloModel();
    private final ObservableList<ChatMessage> masterList = FXCollections.observableArrayList();
    private FilteredList<ChatMessage> filteredList;

    private String getCurrentUsername() {
        String u = usernameField.getText();
        if (u == null) return "Anonymous";
        u = u.trim();
        return u.isEmpty() ? "Anonymous" : u;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        filteredList = new FilteredList<>(masterList, msg -> true);
        chatList.setItems(filteredList);

        chatList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessage msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                    return;
                }

                Text user = new Text(msg.getUsername());
                user.setStyle("-fx-font-weight: bold;");

                Text time = new Text(" (" + msg.getTimestamp() + ")\n");
                time.setStyle("-fx-fill: gray; -fx-font-size: 12px;");

                if (msg.getFileName() != null) {

                    if (msg.getFileUrl() != null && msg.getMimeType() != null) {

                        // Images → inline display
                        if (msg.getMimeType().startsWith("image/")) {
                            ImageView img = new ImageView(new Image(msg.getFileUrl(), true));
                            img.setPreserveRatio(true);
                            img.setFitWidth(250);

                            VBox box = new VBox(
                                    new TextFlow(user, time),
                                    img
                            );
                            setGraphic(box);
                            return;
                        }

                        Hyperlink link = new Hyperlink(msg.getFileName());
                        link.setOnAction(e -> {
                            try {
                                HelloFX.hostServices().showDocument(msg.getFileUrl());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });

                        VBox box = new VBox(
                                new TextFlow(user, time),
                                link
                        );
                        setGraphic(box);
                        return;
                    }


                    if (msg.getFileData() != null) {
                        Text messageText = new Text((msg.getMessage() == null ? "" : msg.getMessage()) + "\n");
                        messageText.setStyle("-fx-font-size: 14px;");

                        Hyperlink fileLink = new Hyperlink(msg.getFileName());
                        final String fileName = msg.getFileName();
                        final String fileData = msg.getFileData();

                        fileLink.setOnAction(event -> {
                            try {
                                byte[] data = Base64.getDecoder().decode(fileData);
                                FileChooser chooser = new FileChooser();
                                chooser.setInitialFileName(fileName);
                                File saveFile = chooser.showSaveDialog(chatList.getScene().getWindow());
                                if (saveFile != null) {
                                    Files.write(saveFile.toPath(), data);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        TextFlow flow = new TextFlow(user, time, messageText, fileLink);
                        setGraphic(flow);
                        return;
                    }
                }

                String body = msg.getMessage() == null ? "" : msg.getMessage();
                Text text = new Text(body);
                text.setStyle("-fx-font-size: 14px;");
                TextFlow flow = new TextFlow(user, time, text);
                setGraphic(flow);
            }
        });

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> updateFilterPredicate());
        hideMyMessagesCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilterPredicate());

        model.loadHistory(msg -> Platform.runLater(() -> masterList.add(msg)));
        model.listenForMessages(msg -> Platform.runLater(() -> masterList.add(msg)));
    }

    private void updateFilterPredicate() {
        final String current = getCurrentUsername();
        final boolean hideMine = hideMyMessagesCheck.isSelected();

        Predicate<ChatMessage> pred = chatMessage -> {
            if (!hideMine) return true;
            String msgUser = chatMessage.getUsername();
            if (msgUser == null) msgUser = "Anonymous";
            return !msgUser.equals(current);
        };

        filteredList.setPredicate(pred);
    }

    @FXML
    private void onSend() {
        String user = getCurrentUsername();
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        final String safeUser = user;
        final String safeMsg = msg;
        inputField.clear();

        new Thread(() -> {
            try {
                model.sendMessage(safeUser, safeMsg);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> masterList.add(new ChatMessage(
                        "system",
                        "[send failed] " + e.getMessage(),
                        java.time.ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )));
            }
        }).start();
    }

    @FXML
    private void onAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to send");
        File file = fileChooser.showOpenDialog(chatList.getScene().getWindow());
        if (file != null) {
            new Thread(() -> model.sendFile(file, getCurrentUsername())).start();
        }
    }
}
