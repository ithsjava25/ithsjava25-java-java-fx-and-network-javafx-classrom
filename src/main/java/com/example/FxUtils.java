package com.example;

import javafx.application.Platform;

public class FxUtils {

    public static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) {
                task.run();
            } else {
                Platform.runLater(task);
            }
        } catch (IllegalStateException notInitialized) {
            //headless
            task.run();
        }
    }
}