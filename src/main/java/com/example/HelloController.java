package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML private Label messageLabel;

    //Nya FXML-komponenter:
    @FXML private ListView<NtfyMessage> messageListView;     //Visar meddelanden från HelloModel
    @FXML private TextField messageTextField;               //Används för att skriva nya meddelanden
    @FXML private Button sendButton;                       //Används för att skicka meddelanden

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
        //Binder listView till meddelandelistan i modellen
        messageListView.setItems(model.getMessages());
    }

    //Ny metod för att hantera knapptryckningar
    @FXML
    private void onSendButtonClick() {
        String messageText = messageTextField.getText();
        if (!messageText.isEmpty()) {
            //Skapa ett nytt meddelande och lägg till det i modellen
            NtfyMessage message = new NtfyMessage(
                    "1",  // ID (kan genereras bättre)
                    System.currentTimeMillis(), // Tid
                    "message", // Eventtyp
                    "chat", // Topic
                    messageText //Meddelandetext
            );
            model.addMessage(message);
            messageTextField.clear(); // Rensa inputfältet
        }

    }
}
