package com.example;

import javafx.application.Platform;

public class FxUtils {

    /**
     * Runs a task on the JavaFX thread.
     * If already on the thread, runs immediately.
     * If not, schedules it to run later.
     * If JavaFX is not initialized, runs on the current thread.
     */

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