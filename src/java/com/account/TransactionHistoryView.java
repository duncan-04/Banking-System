package com.account;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.List;

/** Boundary class: transaction history of the selected account, newest first. */
public class TransactionHistoryView extends VBox {
    private final AccountTransactionController transactionController;
    private final TableView<Transaction> table = new TableView<>();
    private final Label countLabel = new Label();

    public TransactionHistoryView(AccountController accountController,
                                  AccountTransactionController transactionController,
                                  Navigator navigator) {
        this.transactionController = transactionController;
        Account account = accountController.getActiveAccount();

        Label title = new Label("Transaction History");
        title.getStyleClass().add("title-label");
        Label subtitle = UiHelper.styled(account.getAccountType() + "  -  "
                + account.getAccountNumber(), "subtitle-label");

        table.getColumns().add(column("#", 0.07, t -> String.valueOf(t.getTransactionId())));
        table.getColumns().add(column("Date & Time", 0.26, Transaction::getFormattedTime));
        table.getColumns().add(column("Type", 0.20, t -> t.getType().getLabel()));
        table.getColumns().add(column("Description", 0.22, Transaction::getDescription));
        table.getColumns().add(amountColumn());
        table.setPlaceholder(new Label("No transactions on this account yet."));
        table.setPrefHeight(400);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> load());
        Button back = new Button("Back to Dashboard");
        back.getStyleClass().add("secondary-button");
        back.setOnAction(e -> navigator.show(Navigator.Page.DASHBOARD));
        javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(12, refresh, back);

        countLabel.getStyleClass().add("card-muted");

        getStyleClass().add("account-container");
        setSpacing(14);
        setPadding(new Insets(28));
        setAlignment(Pos.TOP_LEFT);
        getChildren().addAll(title, subtitle, table, countLabel, buttons);
        load();
    }

    private void load() {
        List<Transaction> transactions = transactionController.getRecentFirst();
        table.getItems().setAll(transactions);
        countLabel.setText(transactions.size() + " transaction(s)");
    }

    private TableColumn<Transaction, String> column(String name, double share,
                                                    java.util.function.Function<Transaction, String> text) {
        TableColumn<Transaction, String> col = new TableColumn<>(name);
        col.setCellValueFactory(c -> new SimpleStringProperty(text.apply(c.getValue())));
        col.prefWidthProperty().bind(table.widthProperty().multiply(share));
        col.setSortable(false);
        return col;
    }

    private TableColumn<Transaction, String> amountColumn() {
        TableColumn<Transaction, String> col = column("Amount", 0.22,
                t ->(t.isCredit() ? "+" : "-") + Money.format(t.getAmount()));
        col.setCellFactory(c -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("amount-credit", "amount-debit");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                getStyleClass().add(item.startsWith("-") ? "amount-debit" : "amount-credit");
            }
        });
        return col;
    }
}
