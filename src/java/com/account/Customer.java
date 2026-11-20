package com.account;

import java.util.ArrayList;
import java.util.List;
import com.account.database.PasswordHasher;

public class Customer {
    private final String customerId;
    private final String firstname;
    private final String surname;
    private final String address;
    private final String passwordHash;
    private final List<Account> accounts = new ArrayList<>();

    public Customer(String customerId, String firstname, String surname,
                    String address, String password) {
        this.customerId = customerId;
        this.firstname = firstname;
        this.surname = surname;
        this.address = address;
        this.passwordHash = password;
    }

    /** A customer may hold many accounts, of the same or different types. */
    public void addAccount(Account account) {
        if (account == null) return;
        if (account.getOwner() != this) {
            throw new IllegalArgumentException("This account belongs to another customer.");
        }
        if (!accounts.contains(account)) {
            accounts.add(account);
        }
    }

    public List<Account> getAccounts() { return new ArrayList<>(accounts); }
    public boolean hasAccounts() { return !accounts.isEmpty(); }

    public double getTotalBalance() {
        double total = 0;
        for (Account account : accounts) total += account.getBalance();
        return total;
    }

    public boolean verifyPassword(String input) {

        if (input == null || input.isEmpty()) {
            return false;
        }

        return PasswordHasher.verifyPassword(input, passwordHash);
    }

    public String getCustomerId() { return customerId; }
    public String getFirstname() { return firstname; }
    public String getSurname() { return surname; }
    public String getFullName() { return firstname + " " + surname; }
    public String getAddress() { return address; }
    public String getPasswordHash() { return passwordHash; }
}
