package com.example;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import javax.swing.event.HyperlinkListener;
import javax.tools.Tool;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextArea messageInput;

    @FXML private void initialize() {
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
                String text = item.message();
                var att = item.attachment();

                if (att != null && att.url() != null && !att.url().isBlank()) {
                    Label msg = new Label(text != null ? text + " ": "");
                    msg.setWrapText(true);

                    String linkText = att.name() != null && !att.name().isBlank()
                            ? att.name()
                            : "Attachment";
                    Hyperlink link = new Hyperlink(linkText);
                    if (att.size() > 0) {
                        link.setTooltip(new Tooltip(humanSize(att.size()) + (att.type() != null ? " - " + att.type() : "")));
                    }
                    link.setOnAction(e -> openInBrowser(att.url()));

                    HBox row = new HBox(8.0, (Node) msg, (Node) link);
                    row.setFillHeight(true);

                    setText(null);
                    setGraphic(row);
                } else {
                    setText(text != null ? text : "");
                    setGraphic(null);
                }
            }
        });

        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>)
                c -> Platform.runLater(() -> {
                    if (!messageView.getItems(). isEmpty()) {
                        messageView.scrollTo(messageView.getItems().size() - 1);
                    }
                })
        );
        messageView.setOnDragOver(this::handleDragOver);
        messageView.setOnDragDropped(this::handleDragDropped);
        model.loadInitialMessagesAsync();
    }

    @FXML public void sendFile(ActionEvent actionEvent) throws FileNotFoundException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Välj fil att skicka");
        File file = chooser.showOpenDialog(messageView.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            model.sendFile(path);
        }
    }

    public void sendMessage(ActionEvent actionEvent) {
        String text = messageInput != null ? messageInput.getText() : "";
        if (text != null && !text.isBlank() && model.sendMessage(text)) {
            messageInput.clear();
        }
    }

    private void handleDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    private void handleDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            for (File f : files) {
                try {
                    model.sendFile(f.toPath());
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
            success = true;
        }
        e.setDropCompleted(success);
        e.consume();
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                // fallback: ingen Desktop, gör inget (kan ersättas med HostServices)
            }
        } catch (Exception ignored) {
        }
    }

    private static String humanSize(long bytes) {
        // kort & enkel
        String[] units = {"B","KB","MB","GB","TB"};
        double v = bytes;
        int i = 0;
        while (v >= 1024 && i < units.length - 1) { v /= 1024; i++; }
        return String.format("%.1f %s", v, units[i]);
    }
}
