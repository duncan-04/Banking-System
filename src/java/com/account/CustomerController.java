package com.account;

import com.account.Customer;
import com.account.LoginController;

public class CustomerController {

    private LoginController loginController;

    public CustomerController(LoginController loginController) {
        this.loginController = loginController;
    }

    public boolean registerCustomer(String customerId,
                                    String firstName,
                                    String surname,
                                    String address,
                                    String password) {

        // Validate input
        if (customerId == null || customerId.trim().isEmpty()
                || firstName == null || firstName.trim().isEmpty()
                || surname == null || surname.trim().isEmpty()
                || address == null || address.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            return false;
        }

        // Create a new customer
        Customer customer = new Customer(
                customerId,
                firstName,
                surname,
                address,
                password
        );

        // Register customer through LoginController
        return loginController.registerCustomer(customer);
    }

    public Customer findCustomer(String customerId) {
        return null;
    }
}
