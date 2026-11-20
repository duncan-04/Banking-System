package com.account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for every account type.
 * An account cannot exist without the Customer who holds it.
 */
public abstract class Account {

    protected final String accountNumber;
    protected final String branch;
    protected final Customer owner;
    protected double balance;

    protected final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber, String branch,
                      Customer owner, double initialBalance)
            throws BankingException {

        if (owner == null) {
            throw new BankingException(
                    "An account cannot exist without a customer.");
        }

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BankingException("An account number is required.");
        }

        if (!(initialBalance >= 0) || !Double.isFinite(initialBalance)) {
            throw new BankingException(
                    "The initial deposit cannot be negative.");
        }

        this.accountNumber = accountNumber.trim();

        this.branch = (branch == null || branch.isBlank())
                ? "Main Branch" : branch.trim();

        this.owner = owner;
        this.balance = round2(initialBalance);

        // Record the opening balance for a newly created account.
        if (this.balance > 0) {
            addTransaction(
                    TransactionType.OPENING_BALANCE,
                    this.balance,
                    "Opening balance"
            );
        }
    }

    // Account type behaviour

    public abstract AccountType getType();

    public abstract String getFeatureSummary();

    // Deposits

    public void deposit(double amount) throws BankingException {
        deposit(amount, "Deposit");
    }

    public void deposit(double amount, String description)
            throws BankingException {

        requirePositive(amount);

        balance = round2(balance + amount);

        addTransaction(
                TransactionType.DEPOSIT,
                amount,
                description
        );
    }

    // Withdrawals

    public void withdraw(double amount) throws BankingException {

        requirePositive(amount);

        if (amount > balance) {
            throw new BankingException(String.format(
                    "Insufficient funds. Your available balance is BWP %.2f.",
                    balance
            ));
        }

        balance = round2(balance - amount);

        addTransaction(
                TransactionType.WITHDRAWAL,
                amount,
                "Withdrawal"
        );
    }

    public boolean allowsWithdrawals() {
        return true;
    }

    // Interest

    protected void creditInterest(double amount) {

        if (amount > 0 && Double.isFinite(amount)) {

            balance = round2(balance + amount);

            addTransaction(
                    TransactionType.INTEREST,
                    amount,
                    "Monthly interest"
            );
        }
    }

    // Validation and helper methods

    protected static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void requirePositive(double amount)
            throws BankingException {

        if (!(amount > 0) || !Double.isFinite(amount)) {
            throw new BankingException(
                    "The amount must be greater than zero.");
        }
    }

    private void addTransaction(
            TransactionType type,
            double amount,
            String description) {

        transactions.add(
                new Transaction(
                        transactions.size() + 1,
                        type,
                        amount,
                        description,
                        LocalDateTime.now()
                )
        );
    }

    /**
     * Replaces in-memory transaction history with records
     * retrieved from the database.
     */
    public void replaceTransactions(List<Transaction> records) {

        transactions.clear();

        if (records != null) {
            transactions.addAll(records);
        }
    }

    /**
     * Restores the account's previous state if saving a transaction
     * to the database fails.
     */
    public void restoreState(
            double previousBalance,
            List<Transaction> previousTransactions) {

        this.balance = round2(previousBalance);

        transactions.clear();

        if (previousTransactions != null) {
            transactions.addAll(previousTransactions);
        }
    }

    // Account details

    public Map<String, String> getAccountDetails() {
        return new LinkedHashMap<>();
    }

    // Getters

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBranch() {
        return branch;
    }

    public Customer getOwner() {
        return owner;
    }

    public String getAccountType() {
        return getType().getLabel();
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    @Override
    public String toString() {
        return accountNumber + " - " + getAccountType();
    }
}
