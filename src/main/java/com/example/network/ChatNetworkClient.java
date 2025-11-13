package com.example.network;

import com.example.model.NtfyMessage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public interface ChatNetworkClient {
    /**
 * Sends an NtfyMessage to the service at the specified base URL.
 *
 * @param baseUrl the base URL of the ntfy service (e.g., "https://ntfy.example.com")
 * @param message the message to deliver
 * @throws IOException if a network or I/O error occurs while sending the message
 * @throws InterruptedException if the calling thread is interrupted while sending
 */
void send(String baseUrl, NtfyMessage message) throws IOException, InterruptedException;

    /**
 * Sends a file to the specified topic at the given base URL.
 *
 * @param baseUrl the server base URL to send the file to
 * @param topic   the destination topic or channel on the server
 * @param file    the file to upload; it must exist and be readable
 * @throws IOException          if an I/O error occurs while sending the file
 * @throws InterruptedException if the operation is interrupted
 */
void sendFile(String baseUrl, String topic, File file) throws IOException, InterruptedException;

    /**
 * Subscribes to messages for a topic at the specified base URL.
 *
 * @param baseUrl  the base URL of the server to connect to
 * @param topic    the topic to subscribe to
 * @param onMessage callback invoked for each received {@link com.example.model.NtfyMessage}
 * @param onError  callback invoked with a {@link Throwable} if an error occurs while receiving messages
 * @return         an active {@link Subscription} whose {@code close()} method terminates the subscription
 */
Subscription subscribe(String baseUrl, String topic, Consumer<NtfyMessage> onMessage, Consumer<Throwable> onError);

    interface Subscription {
        /**
 * Terminates the active subscription.
 *
 * <p>Closes any underlying resources and stops delivery of further messages for this subscription.</p>
 */
void close();
    }
}