package com.account;

/** Handles "Create Account" on the login screen: registers a brand-new customer. */
public class RegistrationController {
    private final Bank bank;
    private String lastRegisteredCustomerId;

    public RegistrationController(Bank bank) {
        this.bank = bank;
    }

    public OperationResult register(String firstname, String surname, String address,
                                    String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            return OperationResult.fail("The two passwords do not match.");
        }
        try {
            Customer customer = bank.registerCustomer(firstname, surname, address, password);
            lastRegisteredCustomerId = customer.getCustomerId();
            return OperationResult.ok("Registration successful. Your customer ID is "
                    + customer.getCustomerId() + ".");
        } catch (BankingException e) {
            return OperationResult.fail(e.getMessage());
        }
    }

    public String getLastRegisteredCustomerId() { return lastRegisteredCustomerId; }
}
