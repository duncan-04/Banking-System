package com.account.dao;

import com.account.Customer;
import com.account.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO {

    // ==========================================
    // REGISTER CUSTOMER
    // ==========================================

    public boolean registerCustomer(Customer customer, String passwordHash) {

        String sql =
                "INSERT INTO dbo.Customers " +
                        "(CustomerID, FirstName, Surname, Address, PasswordHash) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerId());
            statement.setString(2, customer.getFirstname());
            statement.setString(3, customer.getSurname());
            statement.setString(4, customer.getAddress());
            statement.setString(5, passwordHash);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("CUSTOMER REGISTRATION FAILED:");
            e.printStackTrace();

            return false;
        }
    }


    // ==========================================
    // FIND CUSTOMER
    // ==========================================

    public Customer findCustomer(String customerID) {

        String sql =
                "SELECT CustomerID, FirstName, Surname, Address, PasswordHash " +
                        "FROM dbo.Customers WHERE CustomerID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerID);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return new Customer(
                            result.getString("CustomerID"),
                            result.getString("FirstName"),
                            result.getString("Surname"),
                            result.getString("Address"),
                            result.getString("PasswordHash")
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println("CUSTOMER RETRIEVAL FAILED:");
            e.printStackTrace();
        }

        return null;
    }


    // ==========================================
    // CHECK CUSTOMER EXISTS
    // ==========================================

    public boolean customerExists(String customerID) {

        String sql =
                "SELECT 1 FROM dbo.Customers WHERE CustomerID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerID);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }


    // ==========================================
    // UPDATE CUSTOMER DETAILS
    // ==========================================

    public boolean updateCustomer(
            String customerID,
            String firstname,
            String surname,
            String address) {

        String sql =
                "UPDATE dbo.Customers SET " +
                        "FirstName = ?, Surname = ?, Address = ? " +
                        "WHERE CustomerID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, firstname);
            statement.setString(2, surname);
            statement.setString(3, address);
            statement.setString(4, customerID);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }


    // ==========================================
    // DELETE CUSTOMER
    // ==========================================

    public boolean deleteCustomer(String customerID) {

        String sql =
                "DELETE FROM dbo.Customers WHERE CustomerID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customerID);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Customer could not be deleted.");
            e.printStackTrace();

            return false;
        }
    }


    // ==========================================
    // GENERATE CUSTOMER ID
    // ==========================================

    public String generateCustomerID() {

        String sql =
                "SELECT ISNULL(MAX(TRY_CONVERT(INT, " +
                        "SUBSTRING(CustomerID, 2, 19))), 0) + 1 " +
                        "FROM dbo.Customers";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {

                int nextNumber = result.getInt(1);

                return String.format("C%03d", nextNumber);
            }

        } catch (SQLException e) {

            System.out.println("CUSTOMER ID GENERATION FAILED:");
            e.printStackTrace();
        }

        return null;
    }

}
