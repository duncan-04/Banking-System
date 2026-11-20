package com.account;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

/**
 * The frame shown after login: header with logout, a side panel with navigation
 * and the active-account selector, and a content area that the Navigator swaps.
 */
public class AppShell extends BorderPane {
    private final AccountController accountController;
    private final Navigator navigator;
    private final ComboBox<Account> accountSelector = new ComboBox<>();
    private final Map<Navigator.Page, Button> navButtons = new EnumMap<>(Navigator.Page.class);
    private final ScrollPane contentArea = new ScrollPane();
    private boolean syncing = false;

    public AppShell(AccountController accountController, Navigator navigator) {
        this.accountController = accountController;
        this.navigator = navigator;

        setTop(buildHeader());
        setLeft(buildSidebar());

        contentArea.setFitToWidth(true);
        contentArea.getStyleClass().add("page-scroll");
        setCenter(contentArea);
    }

    private Node buildHeader() {
        Customer customer = accountController.getCustomer();

        Label brand = new Label("BANKING SYSTEM");
        brand.getStyleClass().add("header-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label user = new Label(customer.getFullName() + "  (" + customer.getCustomerId() + ")");
        user.getStyleClass().add("header-user");
        Button logout = new Button("Logout");
        logout.getStyleClass().add("logout-button");
        logout.setOnAction(e -> navigator.logout());

        HBox header = new HBox(16, brand, spacer, user, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 24, 14, 24));
        header.getStyleClass().add("app-header");
        return header;
    }

    private Node buildSidebar() {
        Label selectorCaption = new Label("ACTIVE ACCOUNT");
        selectorCaption.getStyleClass().add("sidebar-caption");
        accountSelector.setPromptText("No accounts yet");
        accountSelector.setMaxWidth(Double.MAX_VALUE);
        accountSelector.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (syncing || newValue == null) return;
            accountController.setActiveAccount(newValue);
            navigator.refresh();
        });
        Label selectorHint = new Label("Deposits, withdrawals and history use this account.");
        selectorHint.getStyleClass().add("sidebar-hint");
        selectorHint.setWrapText(true);

        Label menuCaption = new Label("MENU");
        menuCaption.getStyleClass().add("sidebar-caption");

        VBox sidebar = new VBox(8, selectorCaption, accountSelector, selectorHint,
                spacerOf(12), menuCaption);
        for (Navigator.Page page : Navigator.Page.values()) {
            Button button = new Button(page.getTitle());
            button.getStyleClass().add("nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> navigator.show(page));
            navButtons.put(page, button);
            sidebar.getChildren().add(button);
        }
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);
        sidebar.getStyleClass().add("sidebar");
        return sidebar;
    }

    private Region spacerOf(double height) {
        Region r = new Region();
        r.setMinHeight(height);
        return r;
    }

    /** Called by the Navigator whenever a page is shown. */
    public void showPage(Navigator.Page page, Node content) {
        contentArea.setContent(content);
        contentArea.setVvalue(0);
        for (Map.Entry<Navigator.Page, Button> entry : navButtons.entrySet()) {
            Button button = entry.getValue();
            button.getStyleClass().remove("nav-button-active");
            if (entry.getKey() == page) button.getStyleClass().add("nav-button-active");
            button.setDisable(entry.getKey().requiresAccount() && !accountController.hasAccounts());
        }
        syncAccounts();
    }

    /** Brings the selector up to date with the controller (new account opened, card clicked...). */
    public void syncAccounts() {
        syncing = true;
        try {
            if (!accountSelector.getItems().equals(accountController.getAccounts())) {
                accountSelector.getItems().setAll(accountController.getAccounts());
            }
            if (accountSelector.getValue() != accountController.getActiveAccount()) {
                accountSelector.setValue(accountController.getActiveAccount());
            }
            accountSelector.setDisable(!accountController.hasAccounts());
        } finally {
            syncing = false;
        }
    }
}
