package com.account;

import java.util.Locale;

/** Formats amounts consistently across every screen, e.g. "BWP 1,500.00". */
public final class Money {
    private Money() { }

    public static String format(double amount) {
        return String.format(Locale.US, "BWP %,.2f", amount);
    }
}
