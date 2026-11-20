package com.account;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    private final int transactionId;
    private final TransactionType type;
    private final double amount;
    private final String description;
    private final LocalDateTime time;

    public Transaction(int transactionId, TransactionType type, double amount,
                       String description, LocalDateTime time) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.time = time;
    }

    public int getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDateTime getTime() { return time; }
    public boolean isCredit() { return type.isCredit(); }
    public String getFormattedTime() { return time.format(FORMAT); }

    public String getDetails() {
        return "ID: " + transactionId + " | " + type + " | BWP "
                + String.format("%.2f", amount) + " | " + getFormattedTime();
    }
}
