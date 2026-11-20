package com.account;

import com.account.dao.TransactionDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountTransactionController {

    private final AccountController accountController;
    private final TransactionDAO transactionDAO;

    public AccountTransactionController(
            AccountController accountController) {

        this.accountController = accountController;
        this.transactionDAO = new TransactionDAO();
    }

    // ==========================================
    // GET TRANSACTION HISTORY
    // ==========================================

    public List<Transaction> getTransactionHistory() {

        if (accountController == null
                || accountController.getActiveAccount() == null) {

            return new ArrayList<>();
        }

        Account account = accountController.getActiveAccount();

        // Retrieve transaction records from SQL Server
        List<Transaction> transactions =
                transactionDAO.getTransactions(
                        account.getAccountNumber()
                );

        // Update the account's in-memory transaction history
        account.replaceTransactions(transactions);

        return transactions;
    }


    // ==========================================
    // GET MOST RECENT TRANSACTIONS FIRST
    // ==========================================

    public List<Transaction> getRecentFirst() {

        List<Transaction> transactions =
                new ArrayList<>(getTransactionHistory());

        Collections.reverse(transactions);

        return transactions;
    }


    // ==========================================
    // GET LIMITED TRANSACTION HISTORY
    // ==========================================

    public List<Transaction> getRecentFirst(int limit) {

        List<Transaction> transactions = getRecentFirst();

        if (limit <= 0) {
            return new ArrayList<>();
        }

        return transactions.size() > limit
                ? new ArrayList<>(transactions.subList(0, limit))
                : transactions;
    }


    // ==========================================
    // FIND INDIVIDUAL TRANSACTION
    // ==========================================

    public Transaction getTransactionDetails(int transactionId) {

        for (Transaction transaction : getTransactionHistory()) {

            if (transaction.getTransactionId() == transactionId) {
                return transaction;
            }
        }

        return null;
    }
}
