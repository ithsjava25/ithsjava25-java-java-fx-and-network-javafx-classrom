package com.example;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Handles and returns a list of messages observed by JavaFX
     * Stores, changes and returns data. Remove?
     */
        //Lista som håller alla meddelanden, bindnings-bar och observerbar
            //FXCollections.observableArrayList() = Nyckel som gör listan ändrings-bar och som JavaFX kan lyssna på
        private final ListProperty<String> messages = new SimpleListProperty<>(FXCollections.observableArrayList());

        //getter från private
        public ListProperty<String> getMessages() {
            return messages;
        }
        //tar in ett meddelande från controller och lägger till det i listan
        public void addMessages(String message) {
            messages.add(message);
        }

}
