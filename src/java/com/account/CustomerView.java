package com.account;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class CustomerView extends VBox {

    private final CustomerController customerController;

    private final TextField customerIdField;
    private final TextField firstNameField;
    private final TextField surnameField;
    private final TextField addressField;
    private final PasswordField passwordField;

    private final Label messageLabel;

    private final Consumer<Customer> onRegistrationSuccess;

    public CustomerView(CustomerController customerController,
                        Runnable returnToLogin,
                        Consumer<Customer> onRegistrationSuccess) {

        this.customerController = customerController;
        this.onRegistrationSuccess = onRegistrationSuccess;

        setSpacing(12);
        setPadding(new Insets(25));
        setAlignment(Pos.CENTER);

        // Title
        Label title = new Label("CUSTOMER REGISTRATION");
        title.getStyleClass().add("title-label");

        // Customer ID
        customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID");

        // First Name
        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        // Surname
        surnameField = new TextField();
        surnameField.setPromptText("Surname");

        // Address
        addressField = new TextField();
        addressField.setPromptText("Residential Address");

        // Password
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Buttons
        Button registerButton = new Button("Register");

        Button backButton = new Button("Back to Login");

        // Message
        messageLabel = new Label();

        // Register Button
        registerButton.setOnAction(e -> registerCustomer());

        // Back Button
        backButton.setOnAction(e -> {

            if (returnToLogin != null) {
                returnToLogin.run();
            }
        });

        // Add Components
        getChildren().addAll(
                title,
                customerIdField,
                firstNameField,
                surnameField,
                addressField,
                passwordField,
                registerButton,
                backButton,
                messageLabel
        );
    }

    // Register Customer
    private void registerCustomer() {

        String customerId = customerIdField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String surname = surnameField.getText().trim();
        String address = addressField.getText().trim();
        String password = passwordField.getText();

        // Validate empty fields
        if (customerId.isEmpty()
                || firstName.isEmpty()
                || surname.isEmpty()
                || address.isEmpty()
                || password.isEmpty()) {

            messageLabel.setText("Please complete all fields.");
            return;
        }

        // Register through CustomerController
        boolean registered = customerController.registerCustomer(
                customerId,
                firstName,
                surname,
                address,
                password
        );

        if (registered) {

            messageLabel.setText("Customer registered successfully!");

            // Retrieve the newly registered customer
            Customer customer = customerController.findCustomer(customerId);

            if (customer != null && onRegistrationSuccess != null) {

                onRegistrationSuccess.accept(customer);

            } else {

                messageLabel.setText(
                        "Customer registered, but could not be retrieved."
                );
            }

        } else {

            messageLabel.setText(
                    "Registration failed. Customer ID may already exist."
            );
        }
    }
}