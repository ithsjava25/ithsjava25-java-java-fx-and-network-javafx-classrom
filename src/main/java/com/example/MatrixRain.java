package com.example;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

public class MatrixRain extends Canvas {
    private final int width;
    private final int height;
    private final char[] chars = "01".toCharArray();
    private final int fontSize = 16;
    private final int columns;
    private final int[] drops;
    private final Random random = new Random();


    /**
     * Creates a MatrixRain effect canvas with the specified dimensions
     * @param width the width of the canvas in pixels
     * @param height the height of the canvas in pixels
     */
    public MatrixRain(int width, int height) {
        super(width, height);
        this.width = width;
        this.height = height;
        this.columns = width / fontSize;
        this.drops = new int[columns];
        for (int i = 0; i < columns; i++) drops[i] = 1;
    }

    /**
     * Starts the Matrix digital rain animation effect
     * Creates falling binary digits (0s and 1s) with trailing effects
     */
    public void startAnimation() {
        GraphicsContext gc = getGraphicsContext2D();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.setFill(Color.rgb(0, 0, 0, 0.1));
                gc.fillRect(0, 0, width, height);
                gc.setFill(Color.LIME);
                gc.setFont(javafx.scene.text.Font.font(fontSize));

                for (int i = 0; i < columns; i++) {
                    char c = chars[random.nextInt(chars.length)];
                    gc.fillText(String.valueOf(c), i * fontSize, drops[i] * fontSize);
                    if (drops[i] * fontSize > height && random.nextDouble() > 0.975) drops[i] = 0;
                    drops[i]++;
                }
            }
        }.start();
    }
}