package com.account;

import java.util.Map;

/** Salary account. Only for working customers, so employer details are mandatory. */
public class ChequeAccount extends Account {
    private final String employer;
    private final String employerAddress;

    public ChequeAccount(String accountNumber, String branch, Customer owner,
                         double initialBalance, String employer, String employerAddress)
            throws BankingException {
        super(accountNumber, branch, owner, initialBalance);
        if (employer == null || employer.isBlank()) {
            throw new BankingException(
                    "A Cheque Account is only for working customers. Please enter the company name.");
        }
        if (employerAddress == null || employerAddress.isBlank()) {
            throw new BankingException(
                    "A Cheque Account is only for working customers. Please enter the company address.");
        }
        this.employer = employer.trim();
        this.employerAddress = employerAddress.trim();
    }

    @Override
    public AccountType getType() { return AccountType.CHEQUE; }

    @Override
    public String getFeatureSummary() {
        return "Salary account  |  Employer: " + employer;
    }

    public String getEmployer() { return employer; }
    public String getEmployerAddress() { return employerAddress; }

    @Override
    public Map<String, String> getAccountDetails() {
        Map<String, String> details = super.getAccountDetails();
        details.put("Employer", employer);
        details.put("Employer Address", employerAddress);
        return details;
    }
}
