package com.example;

public class Singelton {
    private final static Singelton instance = new Singelton();

    private Singelton() {}

    public static Singelton getInstance() {
        return instance;
    }
}
