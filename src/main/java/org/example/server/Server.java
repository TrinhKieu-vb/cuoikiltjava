package org.example.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = true;
    private static final int PORT = 5555;
    private static final int THREAD_POOL_SIZE = 20;

    public Server() {
        try {
            this.serverSocket = new ServerSocket(PORT);
            this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            logger.info("Server initialized on port " + PORT);
        } catch (IOException e) {
            logger.error("Error initializing server", e);
        }
    }

    /**
     * Start server and accept client connections
     */
    public void start() {
        logger.info("Server starting on port " + PORT);
        try {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected from " + clientSocket.getInetAddress());

                // Submit client handler to thread pool
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (SocketException e) {
            if (running) {
                logger.error("Socket error", e);
            }
        } catch (IOException e) {
            logger.error("Error accepting client connection", e);
        } finally {
            shutdown();
        }
    }

    /**
     * Shutdown server gracefully
     */
    public void shutdown() {
        logger.info("Server shutting down...");
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            logger.info("Server shutdown complete");
        } catch (IOException e) {
            logger.error("Error during shutdown", e);
        }
    }

    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        Server server = new Server();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));

        server.start();
    }
}

