package com.example;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
        //Lista som håller alla meddelanden
            //FXCollections.observableArrayList() = Nyckel som gör listan ändringsbar och som JavaFX kan lyssna på
        private final ListProperty<String> messages = new SimpleListProperty<>(FXCollections.observableArrayList());

        //getter från private
        public ListProperty<String> messagesProperty() {
            return messages;
        }
        //tar in ett meddelande och lägger till det i listan
        public void addMessage(String message) {
            messages.add(message);
        }

}
