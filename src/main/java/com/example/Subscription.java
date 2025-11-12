package com.example;

import java.io.Closeable;
import java.io.IOException;

public interface Subscription extends Closeable {
    //Stoppar en subscription
    @Override
    void close() throws IOException;

    //Meddelar om Subscription är öppen
    boolean isOpen();
}
