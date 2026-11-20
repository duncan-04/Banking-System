package com.account;

import java.util.Map;

/** Earns 5% interest monthly. Must be opened with at least BWP 500.00. */
public class InvestmentAccount extends Account implements InterestBearing {
    public static final double MINIMUM_OPENING_DEPOSIT = 500.00;
    private static final double MONTHLY_INTEREST_RATE = 0.05;

    public InvestmentAccount(String accountNumber, String branch, Customer owner,
                             double initialBalance) throws BankingException {
        super(accountNumber, branch, owner, initialBalance);
        if (initialBalance < MINIMUM_OPENING_DEPOSIT) {
            throw new BankingException(String.format(
                    "An Investment Account must be opened with at least BWP %.2f.",
                    MINIMUM_OPENING_DEPOSIT));
        }
    }

    @Override
    public AccountType getType() { return AccountType.INVESTMENT; }

    @Override
    public String getFeatureSummary() {
        return "Interest " + getInterestDescription() + "  |  Deposits & withdrawals";
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

    @Override
    public Map<String, String> getAccountDetails() {
        Map<String, String> details = super.getAccountDetails();
        details.put("Interest Rate", getInterestDescription());
        details.put("Minimum Opening Deposit", String.format("BWP %.2f", MINIMUM_OPENING_DEPOSIT));
        return details;
    }
}
