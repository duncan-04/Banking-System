package com.account;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/**
 * Shared layout for "enter an amount and confirm" screens (deposit and withdrawal).
 * It only displays; the work is done by the function the subclass passes in,
 * which calls the AccountController.
 */
public abstract class AmountFormView extends VBox {
    private final AccountController accountController;
    private final Label balanceLabel = new Label();
    private final TextField amountField = new TextField();
    private final Label messageLabel = new Label();

    protected AmountFormView(String heading, String subheading, String confirmText,
                             AccountController accountController, Navigator navigator,
                             Function<String, OperationResult> action, String blockedReason) {
        this.accountController = accountController;
        Account account = accountController.getActiveAccount();

        Label title = new Label(heading);
        title.getStyleClass().add("title-label");
        Label subtitle = UiHelper.styled(subheading, "subtitle-label");

        Label accountName = new Label(account.getAccountType() + "  -  " + account.getAccountNumber());
        accountName.getStyleClass().add("card-title");
        balanceLabel.getStyleClass().add("balance-label");
        VBox accountBox = new VBox(6, UiHelper.styled("SELECTED ACCOUNT", "stat-caption"),
                accountName, balanceLabel);
        accountBox.getStyleClass().add("stat-card");
        accountBox.setMaxWidth(420);
        updateBalance();

        amountField.setPromptText("0.00");
        amountField.setMaxWidth(420);
        Label amountCaption = new Label("Amount (BWP)");
        amountCaption.getStyleClass().add("field-label");

        FlowPane chips = new FlowPane(8, 8);
        chips.setMaxWidth(420);
        for (String quick : new String[]{"100", "500", "1000", "5000"}) {
            Button chip = new Button("BWP " + quick);
            chip.getStyleClass().add("chip-button");
            chip.setOnAction(e -> {
                amountField.setText(quick);
                amountField.requestFocus();
            });
            chips.getChildren().add(chip);
        }

        Button confirmButton = new Button(confirmText);
        Button backButton = new Button("Back to Dashboard");
        backButton.getStyleClass().add("secondary-button");
        messageLabel.getStyleClass().add("message-label");

        Runnable submit = () -> {
            OperationResult result = action.apply(amountField.getText());
            UiHelper.showMessage(messageLabel, result);
            updateBalance();
            if (result.isSuccess()) amountField.clear();
            amountField.requestFocus();
        };
        confirmButton.setOnAction(e -> submit.run());
        amountField.setOnAction(e -> submit.run());
        backButton.setOnAction(e -> navigator.show(Navigator.Page.DASHBOARD));

        if (blockedReason != null) {
            amountField.setDisable(true);
            confirmButton.setDisable(true);
            chips.setDisable(true);
            UiHelper.showMessage(messageLabel, blockedReason, false);
        }

        VBox buttons = new VBox(10, confirmButton, backButton);
        buttons.setMaxWidth(420);
        UiHelper.fillWidth(confirmButton, backButton);

        getStyleClass().add("account-container");
        setSpacing(14);
        setPadding(new Insets(28));
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(title, subtitle, accountBox, amountCaption, amountField,
                chips, messageLabel, buttons);
        messageLabel.setMaxWidth(420);
        if (blockedReason == null) Platform.runLater(amountField::requestFocus);
    }

    private void updateBalance() {
        balanceLabel.setText("Current balance: " + Money.format(accountController.getBalance()));
    }
}
