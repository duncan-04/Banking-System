package com.account;

import com.account.dao.TransactionDAO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountController {

    private final Bank bank;
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private Customer customer;
    private Account activeAccount;

    public AccountController(Bank bank) {
        this.bank = bank;
    }

    
    // SESSION MANAGEMENT
    

    public void startSession(Customer customer) {
        this.customer = customer;

        List<Account> accounts = customer.getAccounts();

        this.activeAccount = accounts.isEmpty()
                ? null : accounts.get(0);
    }

    public void endSession() {
        customer = null;
        activeAccount = null;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Account> getAccounts() {
        return customer == null
                ? new ArrayList<>()
                : customer.getAccounts();
    }

    public boolean hasAccounts() {
        return customer != null && customer.hasAccounts();
    }


    
    // ACTIVE ACCOUNT
    

    public void setActiveAccount(Account account) {

        if (account != null && getAccounts().contains(account)) {
            activeAccount = account;
        }
    }

    public Account getActiveAccount() {
        return activeAccount;
    }

    public double getBalance() {
        return activeAccount == null
                ? 0.0
                : activeAccount.getBalance();
    }

    public double getTotalBalance() {
        return customer == null
                ? 0.0
                : customer.getTotalBalance();
    }

    public boolean canWithdraw() {
        return activeAccount != null
                && activeAccount.allowsWithdrawals();
    }


    
    // DEPOSIT
    

    public OperationResult deposit(String amountText) {

        if (activeAccount == null) {
            return OperationResult.fail(
                    "Please select an account first."
            );
        }

        double previousBalance = activeAccount.getBalance();

        List<Transaction> previousTransactions =
                activeAccount.getTransactions();

        try {

            double amount = parseAmount(amountText);

            // Update Java account object
            activeAccount.deposit(amount);

            // Retrieve the newly created transaction
            List<Transaction> updatedTransactions =
                    activeAccount.getTransactions();

            Transaction transaction =
                    updatedTransactions.get(updatedTransactions.size() - 1);

            // Save transaction and balance to SQL Server
            boolean saved = transactionDAO.saveTransaction(
                    activeAccount.getAccountNumber(),
                    transaction,
                    activeAccount.getBalance()
            );

            if (!saved) {

                // Restore previous account state
                activeAccount.restoreState(
                        previousBalance,
                        previousTransactions
                );

                return OperationResult.fail(
                        "Deposit failed. Your account was not changed. "
                                + "Please try again."
                );
            }

            return OperationResult.ok(
                    "Deposited " + Money.format(amount)
                            + " into " + activeAccount.getAccountNumber()
                            + " successfully."
            );

        } catch (BankingException e) {

            activeAccount.restoreState(
                    previousBalance,
                    previousTransactions
            );

            return OperationResult.fail(e.getMessage());
        }
    }


    
    // WITHDRAWAL
    

    public OperationResult withdraw(String amountText) {

        if (activeAccount == null) {
            return OperationResult.fail(
                    "Please select an account first."
            );
        }

        double previousBalance = activeAccount.getBalance();

        List<Transaction> previousTransactions =
                activeAccount.getTransactions();

        try {

            double amount = parseAmount(amountText);

            // Check account withdrawal rules and update balance
            activeAccount.withdraw(amount);

            // Retrieve the newly created transaction
            List<Transaction> updatedTransactions =
                    activeAccount.getTransactions();

            Transaction transaction =
                    updatedTransactions.get(updatedTransactions.size() - 1);

            // Save transaction and balance to SQL Server
            boolean saved = transactionDAO.saveTransaction(
                    activeAccount.getAccountNumber(),
                    transaction,
                    activeAccount.getBalance()
            );

            if (!saved) {

                // Restore previous account state
                activeAccount.restoreState(
                        previousBalance,
                        previousTransactions
                );

                return OperationResult.fail(
                        "Withdrawal failed. Your account was not changed. "
                                + "Please try again."
                );
            }

            return OperationResult.ok(
                    "Withdrew " + Money.format(amount)
                            + " from " + activeAccount.getAccountNumber()
                            + " successfully."
            );

        } catch (BankingException e) {

            activeAccount.restoreState(
                    previousBalance,
                    previousTransactions
            );

            return OperationResult.fail(e.getMessage());
        }
    }


    
    // OPENING NEW ACCOUNTS
    

    public OperationResult openAccount(
            AccountType type,
            String initialDepositText,
            String employer,
            String employerAddress) {

        if (customer == null) {
            return OperationResult.fail(
                    "Please log in first."
            );
        }

        if (type == null) {
            return OperationResult.fail(
                    "Please choose an account type."
            );
        }

        try {

            double deposit = parseAmount(initialDepositText);

            Account account = type == AccountType.CHEQUE

                    ? bank.openAccount(
                    customer,
                    type,
                    deposit,
                    employer,
                    employerAddress
            )

                    : bank.openAccount(
                    customer,
                    type,
                    deposit
            );

            activeAccount = account;

            return OperationResult.ok(
                    account.getAccountType() + " "
                            + account.getAccountNumber()
                            + " opened successfully."
            );

        } catch (BankingException e) {

            return OperationResult.fail(e.getMessage());
        }
    }


    
    // MONTHLY INTEREST
    

    public OperationResult applyMonthlyInterest() {

        List<String> lines = new ArrayList<>();

        for (Account account : getAccounts()) {

            if (account instanceof InterestBearing) {

                double previousBalance = account.getBalance();

                List<Transaction> previousTransactions =
                        account.getTransactions();

                try {

                    double paid =
                            ((InterestBearing) account).payInterest();

                    if (paid <= 0) {
                        continue;
                    }

                    List<Transaction> updatedTransactions =
                            account.getTransactions();

                    Transaction transaction =
                            updatedTransactions.get(
                                    updatedTransactions.size() - 1
                            );

                    boolean saved = transactionDAO.saveTransaction(
                            account.getAccountNumber(),
                            transaction,
                            account.getBalance()
                    );

                    if (!saved) {

                        account.restoreState(
                                previousBalance,
                                previousTransactions
                        );

                        lines.add(
                                account.getAccountNumber()
                                        + ": interest could not be saved."
                        );

                    } else {

                        lines.add(
                                account.getAccountNumber()
                                        + ": " + Money.format(paid)
                        );
                    }

                } catch (Exception e) {

                    account.restoreState(
                            previousBalance,
                            previousTransactions
                    );

                    lines.add(
                            account.getAccountNumber()
                                    + ": interest payment failed."
                    );
                }
            }
        }

        if (lines.isEmpty()) {
            return OperationResult.fail(
                    "None of your accounts earned interest."
            );
        }

        return OperationResult.ok(
                "Monthly interest processing: "
                        + String.join(", ", lines)
        );
    }


    
    // AMOUNT VALIDATION
    

    private double parseAmount(String text)
            throws BankingException {

        if (text == null || text.isBlank()) {
            throw new BankingException(
                    "Please enter an amount."
            );
        }

        BigDecimal value;

        try {

            value = new BigDecimal(
                    text.trim().replace(",", "")
            );

        } catch (NumberFormatException e) {

            throw new BankingException(
                    "Please enter a valid amount, for example 250.00."
            );
        }

        if (value.scale() > 2
                && value.stripTrailingZeros().scale() > 2) {

            throw new BankingException(
                    "Amounts can have at most 2 decimal places."
            );
        }

        if (value.compareTo(
                new BigDecimal("1000000000")) > 0) {

            throw new BankingException(
                    "That amount is too large."
            );
        }

        return value.doubleValue();
    }
}
