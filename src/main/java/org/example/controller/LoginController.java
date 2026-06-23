package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.client.ClientService;
import org.example.client.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private ClientService clientService = new ClientService();

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
        usernameField.setStyle("-fx-font-size: 14;");
        passwordField.setStyle("-fx-font-size: 14;");
    }

    /**
     * Handle login button click
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập tên đăng nhập và mật khẩu");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        errorLabel.setText("Đang đăng nhập...");
        errorLabel.setStyle("-fx-text-fill: blue;");

        try {
            User user = clientService.login(username, password);
            if (user != null) {
                logger.info("Login successful for user: " + username);
                SessionManager.getInstance().setCurrentUser(user);
                navigateToMainScreen();
            } else {
                errorLabel.setText("Tên đăng nhập hoặc mật khẩu không chính xác");
                errorLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            logger.error("Login error", e);
            errorLabel.setText("Lỗi: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Navigate to main screen after successful login
     */
    private void navigateToMainScreen() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 700);
            stage.setTitle("Quản lý cửa hàng");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error navigating to main screen", e);
            showAlert("Lỗi", "Không thể mở màn hình chính: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}



