package com.account;

/** The kinds of account the bank offers. Used by the GUI and by Bank.openAccount(). */
public enum AccountType {
    SAVINGS("Savings Account",
            "Earns 0.05% interest every month. Deposits only - no withdrawals."),
    INVESTMENT("Investment Account",
            "Earns 5% interest every month. Deposits and withdrawals. Opens with at least BWP 500.00."),
    CHEQUE("Cheque Account",
            "For working customers (salary account). Deposits and withdrawals of any amount. "
                    + "Employer details are required.");

    private final String label;
    private final String description;

    AccountType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return label; }
}
