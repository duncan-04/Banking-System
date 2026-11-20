package com.account;

/** Boundary class: withdraw from the selected account (not available for Savings). */
public class WithdrawalView extends AmountFormView {
    public WithdrawalView(AccountController accountController, Navigator navigator) {
        super("Withdraw Funds",
                "Take money out of your selected account.",
                "Confirm Withdrawal",
                accountController, navigator,
                accountController::withdraw,
                accountController.canWithdraw() ? null
                        : "This " + accountController.getActiveAccount().getAccountType()
                        + " does not allow withdrawals. Choose a different account in the side panel.");
    }
}