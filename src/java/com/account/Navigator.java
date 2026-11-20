package com.account;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Moves the customer between screens. The whole application lives in ONE Scene
 * and only its root changes, so the window never resizes or flickers.
 * Views call the Navigator; they never create other views themselves.
 */
public class Navigator {

    public enum Page {
        DASHBOARD("Dashboard", false),
        MY_ACCOUNT("My Account", true),
        DEPOSIT("Deposit Funds", true),
        WITHDRAW("Withdraw Funds", true),
        HISTORY("Transaction History", true),
        OPEN_ACCOUNT("Open New Account", false);

        private final String title;
        private final boolean requiresAccount;

        Page(String title, boolean requiresAccount) {
            this.title = title;
            this.requiresAccount = requiresAccount;
        }

        public String getTitle() { return title; }

        /** True for pages that only make sense once the customer holds an account. */
        public boolean requiresAccount() { return requiresAccount; }
    }

    private final Stage stage;
    private final Scene scene;
    private final LoginController loginController;
    private final RegistrationController registrationController;
    private final AccountController accountController;
    private final AccountTransactionController transactionController;

    private AppShell shell;
    private Page currentPage = Page.DASHBOARD;

    public Navigator(Stage stage, Scene scene,
                     LoginController loginController,
                     RegistrationController registrationController,
                     AccountController accountController,
                     AccountTransactionController transactionController) {
        this.stage = stage;
        this.scene = scene;
        this.loginController = loginController;
        this.registrationController = registrationController;
        this.accountController = accountController;
        this.transactionController = transactionController;
    }

    // ---------- before login ----------

    public void showLogin() {
        showLogin("", null);
    }

    public void showLogin(String prefillCustomerId, String notice) {
        shell = null;
        stage.setTitle("Banking System - Login");
        scene.setRoot(new LoginView(loginController, this, prefillCustomerId, notice));
    }
    public void showRegistration() {
        stage.setTitle("Banking System - Create Account");
        scene.setRoot(new RegistrationView(registrationController, this));
    }

    // ---------- session ----------

    public void onLoginSuccess() {
        accountController.startSession(loginController.getAuthenticatedCustomer());
        shell = new AppShell(accountController, this);
        show(Page.DASHBOARD);
    }

    public void logout() {
        accountController.endSession();
        loginController.logout();
        showLogin("", "You have been logged out.");
    }

    // ---------- after login ----------

    public void show(Page page) {
        show(page, null);
    }

    public void show(Page page, String notice) {
        if (shell == null) return;

        // A customer with no accounts can still reach the dashboard,
        // but pages that need an account send them to "Open New Account".
        if (!accountController.hasAccounts() && page.requiresAccount()) {
            page = Page.OPEN_ACCOUNT;
            if (notice == null) notice = "Open your first account to get started.";
        }

        currentPage = page;
        Parent content = buildPage(page, notice);
        shell.showPage(page, content);
        stage.setTitle("Banking System - " + page.getTitle());
        if (scene.getRoot() != shell) scene.setRoot(shell);
    }

    /** Rebuilds the current page (used after the active account changes). */
    public void refresh() {
        show(currentPage);
    }

    /** Keeps the account selector in the side panel in step with the controller. */
    public void onActiveAccountChanged() {
        if (shell != null) shell.syncAccounts();
    }

    private Parent buildPage(Page page, String notice) {
        switch (page) {
            case MY_ACCOUNT:   return new AccountView(accountController, this);
            case DEPOSIT:      return new DepositView(accountController, this);
            case WITHDRAW:     return new WithdrawalView(accountController, this);
            case HISTORY:      return new TransactionHistoryView(accountController, transactionController, this);
            case OPEN_ACCOUNT: return new OpenAccountView(accountController, this, notice);
            case DASHBOARD:
            default:           return new DashboardView(accountController, transactionController, this, notice);
        }
    }
}
