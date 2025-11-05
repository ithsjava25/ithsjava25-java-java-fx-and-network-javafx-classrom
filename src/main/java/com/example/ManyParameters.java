package com.example;

public class ManyParameters {

    public ManyParameters(String computerName, int timeout,
                          String method, int size, byte[] data) {

    }


    static void main() {
        ManyParametersBuilder builder = new ManyParametersBuilder();
        builder
                .setComputerName("localhost")
                .setTimeout(10)
                .setSize(0)
                .createManyParameters();
    }
}