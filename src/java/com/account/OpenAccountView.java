package com.account;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Boundary class: lets a customer open any of the three account types.
 * Cheque accounts show extra employer fields. All rules are enforced by the controller.
 */
public class OpenAccountView extends VBox {

    public OpenAccountView(AccountController accountController, Navigator navigator, String notice) {
        Label title = new Label("Open New Account");
        title.getStyleClass().add("title-label");
        Label subtitle = UiHelper.styled(
                "A customer can hold several accounts - choose the type you need.", "subtitle-label");

        Label noticeLabel = new Label();
        noticeLabel.getStyleClass().add("message-label");
        if (notice != null && !notice.isBlank()) UiHelper.showInfo(noticeLabel, notice);

        ComboBox<AccountType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(AccountType.values());
        typeBox.setPromptText("Choose an account type");
        typeBox.setMaxWidth(420);
        typeBox.setPrefWidth(420);
        Label typeInfo = UiHelper.styled("Select a type to see how it works.", "sidebar-hint");
        typeInfo.setMaxWidth(420);

        TextField depositField = new TextField();
        depositField.setPromptText("0.00");
        depositField.setMaxWidth(420);
        Label depositHint = UiHelper.styled("", "sidebar-hint");
        depositHint.setMaxWidth(420);

        TextField employerField = new TextField();
        employerField.setPromptText("Company name");
        employerField.setMaxWidth(420);
        TextField employerAddressField = new TextField();
        employerAddressField.setPromptText("Company address");
        employerAddressField.setMaxWidth(420);
        VBox employerBox = new VBox(8,
                UiHelper.styled("Employer (required for a Cheque Account)", "field-label"), employerField,
                UiHelper.styled("Employer address", "field-label"), employerAddressField);
        employerBox.setVisible(false);
        employerBox.setManaged(false);

        typeBox.valueProperty().addListener((obs, oldType, type) -> {
            typeInfo.setText(type == null ? "" : type.getDescription());
            boolean cheque = type == AccountType.CHEQUE;
            employerBox.setVisible(cheque);
            employerBox.setManaged(cheque);
            depositHint.setText(type == AccountType.INVESTMENT
                    ? "Minimum opening deposit: " + Money.format(InvestmentAccount.MINIMUM_OPENING_DEPOSIT)
                    : "");
        });

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setMaxWidth(420);

        Button openButton = new Button("Open Account");
        Button cancelButton = new Button(accountController.hasAccounts() ? "Back to Dashboard" : "Logout");
        cancelButton.getStyleClass().add("secondary-button");
        UiHelper.fillWidth(openButton, cancelButton);
        VBox buttons = new VBox(10, openButton, cancelButton);
        buttons.setMaxWidth(420);

        Runnable submit = () -> {
            OperationResult result = accountController.openAccount(typeBox.getValue(),
                    depositField.getText(), employerField.getText(), employerAddressField.getText());
            if (result.isSuccess()) {
                navigator.show(Navigator.Page.DASHBOARD, result.getMessage());
            } else {
                UiHelper.showMessage(messageLabel, result);
            }
        };
        openButton.setOnAction(e -> submit.run());
        cancelButton.setOnAction(e -> {
            if (accountController.hasAccounts()) navigator.show(Navigator.Page.DASHBOARD);
            else navigator.logout();
        });

        getStyleClass().add("account-container");
        setSpacing(12);
        setPadding(new Insets(28));
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(title, subtitle, noticeLabel,
                UiHelper.styled("Account type", "field-label"), typeBox, typeInfo,
                UiHelper.styled("Initial deposit (BWP)", "field-label"), depositField, depositHint,
                employerBox, messageLabel, buttons);
        Platform.runLater(typeBox::requestFocus);
    }
}
