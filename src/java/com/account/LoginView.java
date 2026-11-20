package com.account;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Boundary class: collects the customer ID and password. No business logic. */
public class LoginView extends StackPane {

    public LoginView(LoginController loginController, Navigator navigator,
                     String prefillCustomerId, String notice) {
        Label title = new Label("BANKING SYSTEM");
        title.getStyleClass().add("title-label");
        Label subtitle = UiHelper.styled("Sign in to manage your accounts", "subtitle-label");

        TextField userField = new TextField();
        userField.setPromptText("Enter customer ID");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        UiHelper.fillWidth(userField, passField);

        Label message = new Label();
        message.getStyleClass().add("message-label");
        if (notice != null && !notice.isBlank()) UiHelper.showMessage(message, notice, true);

        Button loginButton = new Button("Login");
        Button clearButton = new Button("Clear");
        Button createButton = new Button("Create Account");
        clearButton.getStyleClass().add("secondary-button");
        createButton.getStyleClass().add("secondary-button");
        getStyleClass().add("auth-screen");
        UiHelper.fillWidth(loginButton, clearButton, createButton);
        HBox.setHgrow(clearButton, Priority.ALWAYS);
        HBox.setHgrow(createButton, Priority.ALWAYS);
        HBox secondaryRow = new HBox(10, clearButton, createButton);

        Runnable doLogin = () -> {
            OperationResult result = loginController.login(userField.getText(), passField.getText());
            if (result.isSuccess()) {
                navigator.onLoginSuccess();
            } else {
                UiHelper.showMessage(message, result);
                passField.clear();
                passField.requestFocus();
            }
        };
        loginButton.setOnAction(e -> doLogin.run());
        userField.setOnAction(e -> passField.requestFocus());
        passField.setOnAction(e -> doLogin.run());
        clearButton.setOnAction(e -> {
            userField.clear();
            passField.clear();
            UiHelper.clearMessage(message);
            userField.requestFocus();
        });
        createButton.setOnAction(e -> navigator.showRegistration());

        Label demo = UiHelper.styled("Demo logins:  C001 / 1234    C002 / 5678    C003 / 2468", "sidebar-hint");

        VBox card = new VBox(12, title, subtitle,
                UiHelper.styled("Customer ID", "field-label"), userField,
                UiHelper.styled("Password", "field-label"), passField,
                message, loginButton, secondaryRow, demo);
        card.getStyleClass().add("login-container");
        card.setMaxWidth(430);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPadding(new Insets(36));
        card.setAlignment(Pos.CENTER_LEFT);

        setAlignment(Pos.CENTER);
        setPadding(new Insets(30));
        getChildren().add(card);

        Platform.runLater(() -> {
            if (prefillCustomerId != null && !prefillCustomerId.isBlank()) {
                userField.setText(prefillCustomerId);
                passField.requestFocus();
            } else {
                userField.requestFocus();
            }
        });
    }
}
