package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private StringProperty dateTimeProperty;

    public HelloModel() {
        dateTimeProperty = new SimpleStringProperty();
    }

    public void setDateTime(String dateTime) {
        dateTimeProperty.set(dateTime);
    }

    public String getDateTime() {
        return dateTimeProperty.get();
    }

    public StringProperty dateTimeProperty() {
        return dateTimeProperty;
    }
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    Image noSmash;
    Image smash1;
    Image smash2;
    Image smash3;
    Image smash4;

  /*  public HelloModel() {
        noSmash = new Image();
    }*/

    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }
}
