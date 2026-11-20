package com.account;

public enum TransactionType {
    OPENING_BALANCE("Opening Balance", true),
    DEPOSIT("Deposit", true),
    WITHDRAWAL("Withdrawal", false),
    INTEREST("Interest Payment", true);

    private final String label;
    private final boolean credit;

    TransactionType(String label, boolean credit) {
        this.label = label;
        this.credit = credit;
    }

    public String getLabel() { return label; }

    /** True when the transaction adds money to the account. */
    public boolean isCredit() { return credit; }

    @Override
    public String toString() { return label; }
}