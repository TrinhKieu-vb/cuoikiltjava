package org.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Connect to server
            if (!connectToServer()) {
                showErrorAndExit();
                return;
            }

            // Show login screen
            showLoginScreen(primaryStage);
        } catch (Exception e) {
            logger.error("Error starting application", e);
            showErrorAndExit();
        }
    }

    /**
     * Connect to server
     */
    private boolean connectToServer() {
        NetworkClient client = NetworkClient.getInstance();
        if (client.connect()) {
            logger.info("Connected to server successfully");
            return true;
        } else {
            logger.error("Failed to connect to server");
            return false;
        }
    }

    /**
     * Show login screen
     */
    public void showLoginScreen(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("Quản lý cửa hàng - Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Show main screen
     */
    public void showMainScreen(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1200, 700);
        stage.setTitle("Quản lý cửa hàng");
        stage.setScene(scene);
        stage.show();
    }

    private void showErrorAndExit() {
        logger.error("Cannot connect to server. Exiting...");
        System.exit(1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

