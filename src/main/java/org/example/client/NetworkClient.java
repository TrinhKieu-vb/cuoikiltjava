package org.example.client;

import org.example.model.Request;
import org.example.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NetworkClient {
    private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    private static final int TIMEOUT = 10000; // 10 seconds

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private static NetworkClient instance;

    private NetworkClient() {}

    /**
     * Get singleton instance
     */
    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    /**
     * Connect to server
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            socket.setSoTimeout(TIMEOUT);

            // Initialize streams (output first)
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            logger.info("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            logger.error("Failed to connect to server", e);
            return false;
        }
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
            logger.info("Disconnected from server");
        } catch (IOException e) {
            logger.error("Error disconnecting from server", e);
        }
    }

    /**
     * Check if connected
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Send request and get response
     */
    public Response sendRequest(Request request) {
        try {
            if (!isConnected()) {
                return new Response(false, "503", "Not connected to server", null);
            }

            synchronized (output) {
                output.writeObject(request);
                output.flush();
            }

            synchronized (input) {
                Object response = input.readObject();
                if (response instanceof Response) {
                    return (Response) response;
                }
            }
            return new Response(false, "500", "Invalid response from server", null);
        } catch (SocketTimeoutException e) {
            logger.error("Request timeout", e);
            return new Response(false, "504", "Request timeout", null);
        } catch (IOException e) {
            logger.error("IO error sending request", e);
            return new Response(false, "500", "IO error: " + e.getMessage(), null);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found error", e);
            return new Response(false, "500", "Class not found error", null);
        }
    }
}

