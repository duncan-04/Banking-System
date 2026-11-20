package com.account.dao;

import com.account.*;
import com.account.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    // ==========================================
    // CREATE ACCOUNT AND SAVE OPENING TRANSACTION
    // ==========================================

    public boolean createAccount(Account account) {

        String accountSQL =
                "INSERT INTO dbo.Accounts " +
                        "(AccountNumber, CustomerID, Branch, AccountType, " +
                        "Balance, Employer, EmployerAddress) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String transactionSQL =
                "INSERT INTO dbo.Transactions " +
                        "(AccountID, Amount, TransactionType, Description) " +
                        "SELECT AccountID, ?, ?, ? " +
                        "FROM dbo.Accounts WHERE AccountNumber = ?";

        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Create account

                try (PreparedStatement statement =
                             connection.prepareStatement(accountSQL)) {

                    statement.setString(1, account.getAccountNumber());
                    statement.setString(2, account.getOwner().getCustomerId());
                    statement.setString(3, account.getBranch());
                    statement.setString(4, account.getType().name());

                    statement.setBigDecimal(
                            5,
                            BigDecimal.valueOf(account.getBalance())
                    );

                    if (account instanceof ChequeAccount) {

                        ChequeAccount cheque = (ChequeAccount) account;

                        statement.setString(6, cheque.getEmployer());
                        statement.setString(7, cheque.getEmployerAddress());

                    } else {

                        statement.setNull(6, Types.VARCHAR);
                        statement.setNull(7, Types.VARCHAR);
                    }

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Account creation failed.");
                    }
                }

                // Save opening balance transaction, if one exists

                for (Transaction transaction : account.getTransactions()) {

                    if (transaction.getType()
                            == TransactionType.OPENING_BALANCE) {

                        try (PreparedStatement statement =
                                     connection.prepareStatement(transactionSQL)) {

                            statement.setBigDecimal(
                                    1,
                                    BigDecimal.valueOf(transaction.getAmount())
                            );

                            statement.setString(
                                    2,
                                    transaction.getType().name()
                            );

                            statement.setString(
                                    3,
                                    transaction.getDescription()
                            );

                            statement.setString(
                                    4,
                                    account.getAccountNumber()
                            );

                            if (statement.executeUpdate() != 1) {
                                throw new SQLException(
                                        "Opening transaction could not be saved."
                                );
                            }
                        }
                    }
                }

                connection.commit();
                return true;

            } catch (SQLException e) {

                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println("Account creation failed.");
            e.printStackTrace();
            return false;
        }
    }


    // ==========================================
    // FIND ACCOUNT
    // ==========================================

    public Account findAccount(String accountNumber) {

        String sql =
                "SELECT * FROM dbo.Accounts " +
                        "WHERE AccountNumber = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    CustomerDAO customerDAO = new CustomerDAO();

                    Customer owner = customerDAO.findCustomer(
                            result.getString("CustomerID")
                    );

                    if (owner == null) {
                        return null;
                    }

                    return buildAccount(result, owner);
                }
            }

        } catch (SQLException | BankingException e) {
            e.printStackTrace();
        }

        return null;
    }


    // ==========================================
    // GET CUSTOMER ACCOUNTS
    // ==========================================

    public List<Account> getCustomerAccounts(String customerID) {

        List<Account> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM dbo.Accounts " +
                        "WHERE CustomerID = ? " +
                        "ORDER BY AccountID";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerID);

            try (ResultSet result = statement.executeQuery()) {

                CustomerDAO customerDAO = new CustomerDAO();

                Customer owner = customerDAO.findCustomer(customerID);

                if (owner == null) {
                    return accounts;
                }

                while (result.next()) {

                    Account account = buildAccount(result, owner);

                    accounts.add(account);

                    owner.addAccount(account);
                }
            }

        } catch (SQLException | BankingException e) {
            e.printStackTrace();
        }

        return accounts;
    }


    // ==========================================
    // GET CUSTOMER ACCOUNTS USING CUSTOMER OBJECT
    // ==========================================

    public List<Account> getCustomerAccounts(Customer customer) {

        List<Account> accounts = new ArrayList<>();

        if (customer == null) {
            return accounts;
        }

        String sql =
                "SELECT * FROM dbo.Accounts " +
                        "WHERE CustomerID = ? " +
                        "ORDER BY AccountID";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerId());

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    // Use the actual logged-in customer object
                    Account account = buildAccount(result, customer);

                    accounts.add(account);

                    // Attach account to the logged-in customer
                    customer.addAccount(account);
                }
            }

        } catch (SQLException | BankingException e) {
            e.printStackTrace();
        }

        return accounts;
    }



    // ==========================================
    // UPDATE ACCOUNT BALANCE
    // ==========================================

    public boolean updateBalance(String accountNumber, double balance) {

        if (!Double.isFinite(balance) || balance < 0) {
            return false;
        }

        String sql =
                "UPDATE dbo.Accounts " +
                        "SET Balance = ? " +
                        "WHERE AccountNumber = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(
                    1,
                    BigDecimal.valueOf(balance)
            );

            statement.setString(2, accountNumber);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }


    // ==========================================
    // DELETE ACCOUNT
    // ==========================================

    public boolean deleteAccount(String accountNumber) {

        String sql =
                "DELETE FROM dbo.Accounts " +
                        "WHERE AccountNumber = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Account deletion failed. Check whether transactions exist."
            );

            e.printStackTrace();
            return false;
        }
    }


    // ==========================================
    // BUILD ACCOUNT FROM DATABASE RECORD
    // ==========================================

    private Account buildAccount(ResultSet result, Customer owner)
            throws SQLException, BankingException {

        String accountNumber = result.getString("AccountNumber");
        String branch = result.getString("Branch");
        String type = result.getString("AccountType");

        double balance = result.getBigDecimal("Balance").doubleValue();

        Account account;

        switch (type) {

            case "SAVINGS":

                account = new SavingsAccount(
                        accountNumber,
                        branch,
                        owner,
                        balance
                );

                break;

            case "INVESTMENT":

                account = new InvestmentAccount(
                        accountNumber,
                        branch,
                        owner,
                        balance
                );

                break;

            case "CHEQUE":

                account = new ChequeAccount(
                        accountNumber,
                        branch,
                        owner,
                        balance,
                        result.getString("Employer"),
                        result.getString("EmployerAddress")
                );

                break;

            default:

                throw new BankingException(
                        "Unknown account type: " + type
                );
        }

        // Retrieve actual transaction history from SQL Server

        List<Transaction> storedTransactions =
                transactionDAO.getTransactions(accountNumber);

        // Replace automatically generated opening transaction

        account.replaceTransactions(storedTransactions);

        return account;
    }


    // ==========================================
    // GENERATE UNIQUE ACCOUNT NUMBER
    // ==========================================

    public String generateAccountNumber() {

        return "ACC" +
                java.util.UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 10)
                        .toUpperCase();
    }
}
