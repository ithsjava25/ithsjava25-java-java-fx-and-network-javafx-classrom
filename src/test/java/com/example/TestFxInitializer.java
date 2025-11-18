package com.example;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

public class TestFxInitializer {

    private static boolean initialized = false;

    @BeforeAll
    static void initFx() {
        if (!initialized) {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException ignored) {
                // FX already started
            }
            initialized = true;
        }
    }
}
