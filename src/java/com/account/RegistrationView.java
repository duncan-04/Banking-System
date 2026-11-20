package com.account;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Boundary class: "Create Account" - registers a new customer. No business logic. */
public class RegistrationView extends StackPane {

    public RegistrationView(RegistrationController controller, Navigator navigator) {
        Label title = new Label("CREATE YOUR PROFILE");
        title.getStyleClass().add("title-label");
        Label subtitle = UiHelper.styled(
                "Register first, then open your accounts after you log in.",
                "subtitle-label");

        TextField firstName = new TextField();
        firstName.setPromptText("First name");
        TextField surname = new TextField();
        surname.setPromptText("Surname");
        TextField address = new TextField();
        address.setPromptText("Home address");
        PasswordField password = new PasswordField();
        password.setPromptText("Choose a password (at least " + Bank.MIN_PASSWORD_LENGTH + " characters)");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Re-enter password");
        UiHelper.fillWidth(firstName, surname, address, password, confirm);

        Label message = new Label();
        message.getStyleClass().add("message-label");

        Button createButton = new Button("Create Account");
        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("secondary-button");
        getStyleClass().add("auth-screen");
        UiHelper.fillWidth(createButton, backButton);

        Runnable submit = () -> {
            OperationResult result = controller.register(firstName.getText(), surname.getText(),
                    address.getText(), password.getText(), confirm.getText());
            if (result.isSuccess()) {
                navigator.showLogin(controller.getLastRegisteredCustomerId(),
                        result.getMessage() + " Please log in.");
            } else {
                UiHelper.showMessage(message, result);
            }
        };
        createButton.setOnAction(e -> submit.run());
        confirm.setOnAction(e -> submit.run());
        backButton.setOnAction(e -> navigator.showLogin());

        VBox card = new VBox(12, title, subtitle,
                UiHelper.styled("First name", "field-label"), firstName,
                UiHelper.styled("Surname", "field-label"), surname,
                UiHelper.styled("Address", "field-label"), address,
                UiHelper.styled("Password", "field-label"), password,
                UiHelper.styled("Confirm password", "field-label"), confirm,
                message, createButton, backButton);
        card.getStyleClass().add("login-container");
        card.setMaxWidth(470);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.CENTER_LEFT);

        // Scrolls on small windows instead of squashing the form.
        StackPane centered = new StackPane(card);
        centered.setAlignment(Pos.CENTER);
        centered.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(centered);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("page-scroll");
        getChildren().add(scroll);
        Platform.runLater(firstName::requestFocus);
    }
}
