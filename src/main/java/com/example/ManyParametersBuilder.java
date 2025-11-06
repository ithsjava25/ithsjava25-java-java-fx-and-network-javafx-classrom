package com.example;

public class ManyParametersBuilder {
    private String computerName;
    private int timeout = 0;
    private String method;
    private int size = 0;
    private byte[] data = null;

    public ManyParametersBuilder setComputerName(String computerName) {
        this.computerName = computerName;
        return this;
    }

    public ManyParametersBuilder setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ManyParametersBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public ManyParametersBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    public ManyParametersBuilder setData(byte[] data) {
        this.data = data;
        return this;
    }

    public ManyParameters createManyParameters() {
        return new ManyParameters(computerName, timeout, method, size, data);
    }
}