package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    Image noSmash;
    Image smash1;
    Image smash2;
    Image smash3;
    Image smash4;
    Image background;

    public HelloModel() {
        noSmash = new Image(getClass().getResource("/lantern.png").toExternalForm());
        smash1 = new Image(getClass().getResource("/lantern1.png").toExternalForm());
        smash2 = new Image(getClass().getResource("/lantern2.png").toExternalForm());
        smash3 = new Image(getClass().getResource("/lantern3.png").toExternalForm());
        smash4 = new Image(getClass().getResource("/lantern4.png").toExternalForm());
        background = new Image(getClass().getResource("/background.png").toExternalForm());
    }

    public Image getNoSmash() {
        return noSmash;
    }
    public Image getSmash1() {
        return smash1;
    }
    public Image getSmash2() {
        return smash2;
    }
    public Image getSmash3() {
        return smash3;
    }
    public Image getSmash4() {
        return smash4;
    }
    public Image getBackground() {
        return background;
    }
}
