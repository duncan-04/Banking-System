package com.account;

/**
 * Thrown by the core model when a banking rule is broken
 * (e.g. insufficient funds, withdrawal from a savings account).
 * The message is written for the customer, so a controller can show it as-is.
 */
public class BankingException extends Exception {
    private static final long serialVersionUID = 1L;

    public BankingException(String message) {
        super(message);
    }
}
