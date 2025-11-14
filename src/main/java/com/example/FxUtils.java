package com.example;

import javafx.application.Platform;

public class FxUtils {

    /**
     * Execute task on FX-thread if possible, otherwise inline.
     */
    static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }
}