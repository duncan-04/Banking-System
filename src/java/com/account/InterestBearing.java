package com.account;

/** Implemented by account types that earn interest (Savings and Investment). */
public interface InterestBearing {
    /** Interest that would be paid for one month on the current balance. */
    double calculateInterest();

    /** Pays one month of interest into the account and returns the amount paid. */
    double payInterest();

    /** Monthly rate as a fraction, e.g. 0.05 means 5%. */
    double getInterestRate();

    default String getInterestDescription() {
        return String.format("%.2f%% per month", getInterestRate() * 100);
    }
}
