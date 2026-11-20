package com.account;

import java.util.Map;

/** Earns 0.05% interest monthly. Does NOT allow withdrawals. */
public class SavingsAccount extends Account implements InterestBearing {
    private static final double MONTHLY_INTEREST_RATE = 0.0005;

    public SavingsAccount(String accountNumber, String branch, Customer owner,
                          double initialBalance) throws BankingException {
        super(accountNumber, branch, owner, initialBalance);
    }

    @Override
    public AccountType getType() { return AccountType.SAVINGS; }

    @Override
    public String getFeatureSummary() {
        return "Interest " + getInterestDescription() + "  |  Deposits only";
    }

    @Override
    public double calculateInterest() {
        return round2(balance * MONTHLY_INTEREST_RATE);
    }

    @Override
    public double payInterest() {
        double interest = calculateInterest();
        creditInterest(interest);
        return interest;
    }

    @Override
    public double getInterestRate() { return MONTHLY_INTEREST_RATE; }

    /** Overrides Account.withdraw: savings accounts never allow withdrawals. */
    @Override
    public void withdraw(double amount) throws BankingException {
        throw new BankingException("Savings accounts do not allow withdrawals.");
    }

    @Override
    public boolean allowsWithdrawals() { return false; }

    @Override
    public Map<String, String> getAccountDetails() {
        Map<String, String> details = super.getAccountDetails();
        details.put("Interest Rate", getInterestDescription());
        details.put("Withdrawals", "Not allowed");
        return details;
    }
}
