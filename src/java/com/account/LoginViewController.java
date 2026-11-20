package com.account;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginViewController {

    @FXML
    private TextField customerIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private LoginController loginController;
    private Navigator navigator;

    // Connect the FXML screen to the existing application controllers
    public void setDependencies(
            LoginController loginController,
            Navigator navigator,
            String prefillCustomerId,
            String notice) {

        this.loginController = loginController;
        this.navigator = navigator;

        if (prefillCustomerId != null && !prefillCustomerId.isBlank()) {
            customerIdField.setText(prefillCustomerId);
        }

        if (notice != null && !notice.isBlank()) {
            messageLabel.setText(notice);
        }
    }

    @FXML
    private void handleLogin() {

        String customerId = customerIdField.getText().trim();
        String password = passwordField.getText();

        if (customerId.isEmpty() || password.isEmpty()) {
            messageLabel.setText(
                    "Please enter both your customer ID and password."
            );
            return;
        }

        // Use your existing LoginController
        OperationResult result =
                loginController.login(customerId, password);

        messageLabel.setText(result.getMessage());

        if (result.isSuccess()) {

            passwordField.clear();

            // Navigate to the dashboard
            navigator.onLoginSuccess();

        } else {
            passwordField.clear();
        }
    }

    @FXML
    private void handleClear() {

        customerIdField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }

    @FXML
    private void handleCreateAccount() {

        // Navigate to your existing registration screen
        navigator.showRegistration();
    }
}