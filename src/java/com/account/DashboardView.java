package com.account;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Boundary class: the customer's home screen. Shows every account as a card the
 * customer can click to select, plus quick actions and recent activity.
 */
public class DashboardView extends VBox {
    private final AccountController accountController;
    private final AccountTransactionController transactionController;
    private final Navigator navigator;

    private final Label totalValue = new Label();
    private final Label countValue = new Label();
    private final FlowPane accountCards = new FlowPane(16, 16);
    private final Button withdrawButton = new Button("Withdraw Funds");
    private final Button depositButton = new Button("Deposit Funds");
    private final Button historyButton = new Button("Transaction History");
    private final Button detailsButton = new Button("Account Details");
    private final Button interestButton = new Button("Apply Monthly Interest");
    private final Label hintLabel = new Label();
    private final Label messageLabel = new Label();
    private final Label recentHeading = new Label();
    private final VBox recentBox = new VBox(6);

    public DashboardView(AccountController accountController,
                         AccountTransactionController transactionController,
                         Navigator navigator, String notice) {
        this.accountController = accountController;
        this.transactionController = transactionController;
        this.navigator = navigator;

        Label title = new Label("Dashboard");
        title.getStyleClass().add("title-label");
        Label welcome = UiHelper.styled("Welcome back, "
                + accountController.getCustomer().getFullName(), "subtitle-label");

        // summary cards
        VBox totalCard = statCard("TOTAL BALANCE (ALL ACCOUNTS)", totalValue);
        VBox countCard = statCard("ACCOUNTS HELD", countValue);
        HBox summary = new HBox(16, totalCard, countCard);

        // accounts
        Label accountsHeading = new Label("Your accounts");
        accountsHeading.getStyleClass().add("section-label");
        Label accountsHint = UiHelper.styled(
                "Click an account to select it. Deposits, withdrawals and history apply to the selected account.",
                "sidebar-hint");

        // quick actions
        Label actionsHeading = new Label("Quick actions");
        actionsHeading.getStyleClass().add("section-label");
        depositButton.setOnAction(e -> navigator.show(Navigator.Page.DEPOSIT));
        withdrawButton.setOnAction(e -> navigator.show(Navigator.Page.WITHDRAW));
        historyButton.setOnAction(e -> navigator.show(Navigator.Page.HISTORY));
        detailsButton.setOnAction(e -> navigator.show(Navigator.Page.MY_ACCOUNT));
        interestButton.setOnAction(e -> {
            UiHelper.showMessage(messageLabel, accountController.applyMonthlyInterest());
            renderAll();
        });
        Button openButton = new Button("Open New Account");
        openButton.setOnAction(e -> navigator.show(Navigator.Page.OPEN_ACCOUNT));
        FlowPane actions = new FlowPane(12, 12, depositButton, withdrawButton, historyButton,
                detailsButton, interestButton, openButton);

        hintLabel.getStyleClass().add("sidebar-hint");
        messageLabel.getStyleClass().add("message-label");
        if (notice != null && !notice.isBlank()) UiHelper.showMessage(messageLabel, notice, true);

        recentHeading.getStyleClass().add("section-label");

        getStyleClass().add("account-container");
        setSpacing(14);
        setPadding(new Insets(28));
        getChildren().addAll(title, welcome, summary, messageLabel,
                accountsHeading, accountsHint, accountCards,
                actionsHeading, actions, hintLabel,
                recentHeading, recentBox);
        renderAll();
    }

    private VBox statCard(String caption, Label value) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("stat-caption");
        value.getStyleClass().add("stat-value");
        VBox card = new VBox(6, captionLabel, value);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(300);
        return card;
    }

    private void renderAll() {
        totalValue.setText(Money.format(accountController.getTotalBalance()));
        countValue.setText(String.valueOf(accountController.getAccounts().size()));
        renderAccountCards();
        renderActionState();
        renderRecentActivity();
    }

    private void renderAccountCards() {
        accountCards.getChildren().clear();
        Account active = accountController.getActiveAccount();

        for (Account account : accountController.getAccounts()) {
            Label type = new Label(account.getAccountType());
            type.getStyleClass().add("card-title");
            Label number = new Label("Account " + account.getAccountNumber());
            number.getStyleClass().add("card-muted");
            Label balance = new Label(Money.format(account.getBalance()));
            balance.getStyleClass().add("card-balance");
            Label features = UiHelper.styled(account.getFeatureSummary(), "card-muted");

            VBox card = new VBox(6, type, number, balance, features);
            card.getStyleClass().add("account-card");
            if (account == active) {
                card.getStyleClass().add("account-card-selected");
                Label selected = new Label("SELECTED");
                selected.getStyleClass().add("selected-badge");
                card.getChildren().add(0, selected);
            }
            card.setPrefWidth(228);
            card.setOnMouseClicked(e -> {
                accountController.setActiveAccount(account);
                navigator.onActiveAccountChanged();
                renderAll();
            });
            accountCards.getChildren().add(card);
        }

        Label plus = new Label("+");
        plus.getStyleClass().add("new-card-plus");
        Label newText = new Label(accountController.hasAccounts()
                ? "Open a new account" : "Open your first account");
        newText.getStyleClass().add("card-title");
        VBox newCard = new VBox(4, plus, newText);
        newCard.setAlignment(Pos.CENTER);
        newCard.getStyleClass().addAll("account-card", "account-card-new");
        newCard.setPrefWidth(228);
        newCard.setOnMouseClicked(e -> navigator.show(Navigator.Page.OPEN_ACCOUNT));
        accountCards.getChildren().add(newCard);
    }

    private void renderActionState() {
        boolean has = accountController.hasAccounts();
        depositButton.setDisable(!has);
        historyButton.setDisable(!has);
        detailsButton.setDisable(!has);
        interestButton.setDisable(!has);
        withdrawButton.setDisable(!has || !accountController.canWithdraw());

        Account active = accountController.getActiveAccount();
        if (has && !accountController.canWithdraw()) {
            hintLabel.setText("Withdrawals are not available on the selected " + active.getAccountType()
                    + ". Select another account to withdraw.");
        } else if (!has) {
            hintLabel.setText("You have no accounts yet. Open one to start depositing.");
        } else {
            hintLabel.setText("");
        }
    }

    private void renderRecentActivity() {
        recentBox.getChildren().clear();
        Account active = accountController.getActiveAccount();
        if (active == null) {
            recentHeading.setText("");
            return;
        }
        recentHeading.setText("Recent activity - " + active.getAccountNumber());

        List<Transaction> recent = transactionController.getRecentFirst(5);
        if (recent.isEmpty()) {
            recentBox.getChildren().add(UiHelper.styled("No transactions on this account yet.", "card-muted"));
            return;
        }
        for (Transaction tx : recent) {
            Label what = new Label(tx.getType().getLabel());
            what.getStyleClass().add("activity-type");
            Label when = new Label(tx.getFormattedTime());
            when.getStyleClass().add("card-muted");
            Label amount = new Label((tx.isCredit() ? "+" : "-") + Money.format(tx.getAmount()));
            amount.getStyleClass().add(tx.isCredit() ? "amount-credit" : "amount-debit");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox row = new HBox(14, what, when, spacer, amount);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("activity-row");
            recentBox.getChildren().add(row);
        }
    }
}
