package com.account;

import java.util.HashMap;
import java.util.Map;

/**
 * Authenticates customers. Failed attempts are counted PER customer ID, so one
 * person mistyping a password cannot lock everybody else out.
 */
public class LoginController {
    public static final int MAX_ATTEMPTS = 3;

    private final Bank bank;
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private Customer authenticatedCustomer;

    public LoginController(Bank bank) {
        this.bank = bank;
    }

    public OperationResult login(String customerId, String password) {
        if (customerId == null || customerId.isBlank() || password == null || password.isEmpty()) {
            return OperationResult.fail("Please enter both your customer ID and password.");
        }
        String key = customerId.trim().toUpperCase();

        if (isLocked(key)) {
            return OperationResult.fail("This customer ID is locked after "
                    + MAX_ATTEMPTS + " failed attempts. Please contact the bank.");
        }

        Customer customer = bank.findCustomer(key);
        if (customer != null && customer.verifyPassword(password)) {
            authenticatedCustomer = customer;
            failedAttempts.remove(key);
            return OperationResult.ok("Welcome, " + customer.getFirstname() + "!");
        }

        int attempts = failedAttempts.merge(key, 1, Integer::sum);
        if (attempts >= MAX_ATTEMPTS) {
            return OperationResult.fail("Too many failed attempts. This customer ID is now locked.");
        }
        // Same message whether the ID or the password was wrong (does not reveal which IDs exist).
        return OperationResult.fail("Invalid customer ID or password. "
                + (MAX_ATTEMPTS - attempts) + " attempt(s) left.");
    }

    public boolean isLocked(String customerId) {
        if (customerId == null) return false;
        return failedAttempts.getOrDefault(customerId.trim().toUpperCase(), 0) >= MAX_ATTEMPTS;
    }

    public Customer getAuthenticatedCustomer() {
        return authenticatedCustomer;
    }

    public void logout() {
        authenticatedCustomer = null;
    }


    public boolean registerCustomer(Customer customer) {
        return false;
    }
}


