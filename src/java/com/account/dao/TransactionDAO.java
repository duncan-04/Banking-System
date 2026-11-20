package com.account.dao;

import com.account.Transaction;
import com.account.TransactionType;
import com.account.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // ==========================================
    // SAVE TRANSACTION AND UPDATE ACCOUNT BALANCE
    // ==========================================

    public boolean saveTransaction(
            String accountNumber,
            Transaction transaction,
            double newBalance) {

        if (accountNumber == null || accountNumber.isBlank()
                || transaction == null
                || !Double.isFinite(newBalance)
                || newBalance < 0
                || !Double.isFinite(transaction.getAmount())
                || transaction.getAmount() <= 0) {

            System.err.println("Invalid transaction information.");
            return false;
        }

        String updateBalanceSQL =
                "UPDATE dbo.Accounts " +
                        "SET Balance = ? " +
                        "WHERE AccountNumber = ?";

        String insertTransactionSQL =
                "INSERT INTO dbo.Transactions " +
                        "(AccountID, Amount, TransactionType, Description) " +
                        "SELECT AccountID, ?, ?, ? " +
                        "FROM dbo.Accounts " +
                        "WHERE AccountNumber = ?";

        Connection connection = null;

        try {

            connection = DatabaseManager.getConnection();

            // Begin database transaction
            connection.setAutoCommit(false);

            // STEP 1: Update account balance

            try (PreparedStatement updateStatement =
                         connection.prepareStatement(updateBalanceSQL)) {

                updateStatement.setBigDecimal(
                        1,
                        BigDecimal.valueOf(newBalance)
                );

                updateStatement.setString(2, accountNumber);

                int updatedRows = updateStatement.executeUpdate();

                if (updatedRows != 1) {
                    throw new SQLException(
                            "Account balance could not be updated."
                    );
                }
            }

            // STEP 2: Insert transaction record

            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertTransactionSQL)) {

                insertStatement.setBigDecimal(
                        1,
                        BigDecimal.valueOf(transaction.getAmount())
                );

                insertStatement.setString(
                        2,
                        transaction.getType().name()
                );

                insertStatement.setString(
                        3,
                        transaction.getDescription()
                );

                insertStatement.setString(
                        4,
                        accountNumber
                );

                int insertedRows = insertStatement.executeUpdate();

                if (insertedRows != 1) {
                    throw new SQLException(
                            "Transaction record could not be inserted."
                    );
                }
            }

            // STEP 3: Commit both operations
            connection.commit();

            return true;

        } catch (SQLException e) {

            System.out.println(
                    "Transaction failed. Rolling back database changes."
            );

            e.printStackTrace();

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            return false;

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                    connection.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // ==========================================
    // GET ALL TRANSACTIONS FOR AN ACCOUNT
    // ==========================================

    public List<Transaction> getTransactions(String accountNumber) {

        List<Transaction> transactions = new ArrayList<>();

        String sql =
                "SELECT " +
                        "t.TransactionID, " +
                        "t.Amount, " +
                        "t.TransactionType, " +
                        "t.Description, " +
                        "t.TransactionTime " +
                        "FROM dbo.Transactions t " +
                        "INNER JOIN dbo.Accounts a " +
                        "ON t.AccountID = a.AccountID " +
                        "WHERE a.AccountNumber = ? " +
                        "ORDER BY t.TransactionTime ASC, t.TransactionID ASC";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Timestamp timestamp =
                            result.getTimestamp("TransactionTime");

                    LocalDateTime transactionTime =
                            timestamp == null
                                    ? LocalDateTime.now()
                                    : timestamp.toLocalDateTime();

                    Transaction transaction = new Transaction(

                            result.getInt("TransactionID"),

                            TransactionType.valueOf(
                                    result.getString("TransactionType")
                            ),

                            result.getBigDecimal("Amount").doubleValue(),

                            result.getString("Description"),

                            transactionTime
                    );

                    transactions.add(transaction);
                }
            }

        } catch (SQLException | IllegalArgumentException e) {

            System.out.println(
                    "Could not retrieve transaction history."
            );

            e.printStackTrace();
        }

        return transactions;
    }


    // ==========================================
    // FIND INDIVIDUAL TRANSACTION
    // ==========================================

    public Transaction findTransaction(int transactionID) {

        String sql =
                "SELECT TransactionID, Amount, TransactionType, " +
                        "Description, TransactionTime " +
                        "FROM dbo.Transactions " +
                        "WHERE TransactionID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, transactionID);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    Timestamp timestamp =
                            result.getTimestamp("TransactionTime");

                    LocalDateTime transactionTime =
                            timestamp == null
                                    ? LocalDateTime.now()
                                    : timestamp.toLocalDateTime();

                    return new Transaction(

                            result.getInt("TransactionID"),

                            TransactionType.valueOf(
                                    result.getString("TransactionType")
                            ),

                            result.getBigDecimal("Amount").doubleValue(),

                            result.getString("Description"),

                            transactionTime
                    );
                }
            }

        } catch (SQLException | IllegalArgumentException e) {

            System.out.println(
                    "Could not find transaction."
            );

            e.printStackTrace();
        }

        return null;
    }
}