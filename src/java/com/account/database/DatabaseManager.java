package com.account.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;" +
                    "databaseName=BankingSystem;" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;";

    private static final String USER = "BankingAdmin";

    private static final String PASSWORD = "2006";


    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }


    public static void main(String[] args) {

        try (Connection connection = getConnection()) {

            System.out.println("Database Successfully connected to SQL Server!");

        } catch (SQLException e) {

            System.out.println("Database connection failed!");

            e.printStackTrace();
        }
    }
}
