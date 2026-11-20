package com.account;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/** Boundary class: read-only details of the selected account and its holder. */
public class AccountView extends VBox {

    public AccountView(AccountController accountController, Navigator navigator) {
        Account account = accountController.getActiveAccount();
        Customer customer = accountController.getCustomer();

        Label title = new Label("My Account");
        title.getStyleClass().add("title-label");
        Label subtitle = UiHelper.styled("Details of your selected account.", "subtitle-label");

        Map<String, String> rows = new LinkedHashMap<>();
        rows.put("Account Holder", customer.getFullName());
        rows.put("Customer ID", customer.getCustomerId());
        rows.put("Address", customer.getAddress());
        rows.put("Account Number", account.getAccountNumber());
        rows.put("Account Type", account.getAccountType());
        rows.put("Branch", account.getBranch());
        rows.put("Current Balance", Money.format(account.getBalance()));
        rows.putAll(account.getAccountDetails());   // each account type adds its own facts
        rows.put("Transactions", String.valueOf(account.getTransactions().size()));

        GridPane grid = new GridPane();
        grid.getStyleClass().add("account-grid");
        grid.setHgap(30);
        grid.setVgap(14);
        grid.setMaxWidth(560);
        int row = 0;
        for (Map.Entry<String, String> entry : rows.entrySet()) {
            Label key = new Label(entry.getKey());
            key.getStyleClass().add("grid-key");
            Label value = new Label(entry.getValue());
            value.getStyleClass().add("grid-value");
            value.setWrapText(true);
            grid.add(key, 0, row);
            grid.add(value, 1, row);
            row++;
        }

        Button history = new Button("View Transactions");
        history.setOnAction(e -> navigator.show(Navigator.Page.HISTORY));
        Button back = new Button("Back to Dashboard");
        back.getStyleClass().add("secondary-button");
        back.setOnAction(e -> navigator.show(Navigator.Page.DASHBOARD));
        HBox buttons = new HBox(12, history, back);

        getStyleClass().add("account-container");
        setSpacing(16);
        setPadding(new Insets(28));
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(title, subtitle, grid, buttons);
    }
}
