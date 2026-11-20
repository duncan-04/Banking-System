package com.account;

import com.account.dao.CustomerDAO;
import com.account.dao.AccountDAO;
import com.account.database.PasswordHasher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Bank {

    public static final int MIN_PASSWORD_LENGTH = 4;

    private final String bankName;
    private final String branchName;

    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    // Database access objects
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public Bank(String bankName, String branchName) {
        this.bankName = bankName;
        this.branchName = branchName;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBranchName() {
        return branchName;
    }


    // ==========================================
    // CUSTOMER REGISTRATION
    // ==========================================

    public Customer registerCustomer(
            String firstname,
            String surname,
            String address,
            String password
    ) throws BankingException {

        if (isBlank(firstname) || isBlank(surname) || isBlank(address)) {
            throw new BankingException(
                    "Please enter your first name, surname and address."
            );
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BankingException(
                    "Your password must be at least "
                            + MIN_PASSWORD_LENGTH + " characters long."
            );
        }

        try {

            // Generate customer ID
            String id = customerDAO.generateCustomerID();

            if (id == null || id.isBlank()) {
                throw new BankingException(
                        "Could not generate customer ID. Please try again."
                );
            }

            // Hash password
            String hashedPassword =
                    PasswordHasher.hashPassword(password);

            // Create customer object
            Customer customer = new Customer(
                    id,
                    firstname.trim(),
                    surname.trim(),
                    address.trim(),
                    hashedPassword
            );

            // Save customer to SQL Server
            boolean saved = customerDAO.registerCustomer(
                    customer,
                    hashedPassword
            );

            if (!saved) {
                throw new BankingException(
                        "Customer registration failed. Please try again."
                );
            }

            customers.put(id, customer);

            return customer;

        } catch (BankingException e) {
            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new BankingException(
                    "An error occurred while registering the customer."
            );
        }
    }


    // ==========================================
    // FIND CUSTOMER AND LOAD THEIR ACCOUNTS
    // ==========================================

    public Customer findCustomer(String customerId) {

        if (customerId == null || customerId.isBlank()) {
            return null;
        }

        String id = customerId.trim().toUpperCase();

        try {

            // Retrieve customer from SQL Server
            Customer customer = customerDAO.findCustomer(id);

            if (customer == null) {
                return null;
            }

            // Remove previously cached accounts belonging to this customer
            accounts.entrySet().removeIf(
                    entry -> entry.getValue().getOwner()
                            .getCustomerId().equalsIgnoreCase(id)
            );

            // Load accounts using the actual customer object
            List<Account> customerAccounts =
                    accountDAO.getCustomerAccounts(customer);

            // Store retrieved accounts in application memory
            for (Account account : customerAccounts) {

                accounts.put(
                        account.getAccountNumber(),
                        account
                );
            }

            // Store the customer in application memory
            customers.put(id, customer);

            return customer;

        } catch (Exception e) {

            System.err.println("CUSTOMER RETRIEVAL FAILED:");

            e.printStackTrace();

            return null;
        }
    }


    // ==========================================
    // OPEN SAVINGS OR INVESTMENT ACCOUNT
    // ==========================================

    public Account openAccount(
            Customer customer,
            AccountType type,
            double initialDeposit
    ) throws BankingException {

        return openAccount(
                customer,
                type,
                initialDeposit,
                null,
                null
        );
    }


    // ==========================================
    // OPEN ANY ACCOUNT TYPE
    // ==========================================

    public Account openAccount(
            Customer customer,
            AccountType type,
            double initialDeposit,
            String employer,
            String employerAddress
    ) throws BankingException {

        // Validate customer
        if (customer == null ||
                customers.get(customer.getCustomerId()) != customer) {

            throw new BankingException(
                    "Only registered customers can open an account."
            );
        }

        if (type == null) {
            throw new BankingException(
                    "Please choose an account type."
            );
        }

        if (!Double.isFinite(initialDeposit) || initialDeposit < 0) {
            throw new BankingException(
                    "Please enter a valid initial deposit."
            );
        }

        // Generate account number
        String accountNumber = accountDAO.generateAccountNumber();

        if (accountNumber == null || accountNumber.isBlank()) {
            throw new BankingException(
                    "Could not generate an account number."
            );
        }

        Account account;

        // Create appropriate account type
        switch (type) {

            case SAVINGS:

                account = new SavingsAccount(
                        accountNumber,
                        branchName,
                        customer,
                        initialDeposit
                );

                break;

            case INVESTMENT:

                account = new InvestmentAccount(
                        accountNumber,
                        branchName,
                        customer,
                        initialDeposit
                );

                break;

            case CHEQUE:

                account = new ChequeAccount(
                        accountNumber,
                        branchName,
                        customer,
                        initialDeposit,
                        employer,
                        employerAddress
                );

                break;

            default:

                throw new BankingException(
                        "Unknown account type."
                );
        }

        // Save account and opening transaction to SQL Server
        boolean saved = accountDAO.createAccount(account);

        if (!saved) {
            throw new BankingException(
                    "Account creation failed. Please try again."
            );
        }

        // Store account in application memory
        accounts.put(accountNumber, account);

        // Attach account to the correct customer
        customer.addAccount(account);

        return account;
    }


    // ==========================================
    // FIND ACCOUNT
    // ==========================================

    public Account findAccount(String accountNumber) {

        if (accountNumber == null || accountNumber.isBlank()) {
            return null;
        }

        String number = accountNumber.trim();

        // Check application memory first
        Account account = accounts.get(number);

        if (account != null) {
            return account;
        }

        // Retrieve account from SQL Server
        account = accountDAO.findAccount(number);

        if (account != null) {

            accounts.put(number, account);

            Customer owner = account.getOwner();

            customers.put(owner.getCustomerId(), owner);
        }

        return account;
    }


    // ==========================================
    // GET ALL ACCOUNTS
    // ==========================================

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }


    // ==========================================
    // DEMO DATA
    // ==========================================

    public void seedDemoData() {

        try {

            Customer john = registerCustomer(
                    "John",
                    "Doe",
                    "123 Main St, Gaborone",
                    "1234"
            );

            openAccount(
                    john,
                    AccountType.SAVINGS,
                    1500.00
            );

            openAccount(
                    john,
                    AccountType.INVESTMENT,
                    2500.00
            );

            Customer jane = registerCustomer(
                    "Jane",
                    "Smith",
                    "45 River Road, Francistown",
                    "5678"
            );

            openAccount(
                    jane,
                    AccountType.INVESTMENT,
                    5000.00
            );

            Customer peter = registerCustomer(
                    "Peter",
                    "Jones",
                    "8 Mall Road, Maun",
                    "2468"
            );

            openAccount(
                    peter,
                    AccountType.CHEQUE,
                    3200.00,
                    "ABC Company",
                    "Plot 12, Gaborone"
            );

        } catch (BankingException e) {

            throw new IllegalStateException(
                    "Demo data is invalid: " + e.getMessage(),
                    e
            );
        }
    }


    // ==========================================
    // VALIDATION
    // ==========================================

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
