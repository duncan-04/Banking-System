package com.account;

/** Boundary class: deposit into the selected account. */
public class DepositView extends AmountFormView {
    public DepositView(AccountController accountController, Navigator navigator) {
        super("Deposit Funds",
                "Add money to your selected account.",
                "Confirm Deposit",
                accountController, navigator,
                accountController::deposit,
                null);
    }
}