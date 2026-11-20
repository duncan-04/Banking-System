package com.account;

/** What a controller hands back to a view: did it work, and what to tell the customer. */
public class OperationResult {
    private final boolean success;
    private final String message;

    private OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static OperationResult ok(String message) { return new OperationResult(true, message); }
    public static OperationResult fail(String message) { return new OperationResult(false, message); }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}