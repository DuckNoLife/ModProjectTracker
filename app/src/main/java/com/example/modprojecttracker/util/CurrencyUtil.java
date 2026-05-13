package com.example.modprojecttracker.util;

import java.util.HashMap;
import java.util.Map;

public class CurrencyUtil {
    private static final Map<String, Double> TO_USD_RATES = new HashMap<>();

    static {
        TO_USD_RATES.put(Constants.CURRENCY_USD, 1.0);
        TO_USD_RATES.put(Constants.CURRENCY_EUR, 1.08);
        TO_USD_RATES.put(Constants.CURRENCY_GBP, 1.27);
        TO_USD_RATES.put(Constants.CURRENCY_JPY, 0.0064);
        TO_USD_RATES.put(Constants.CURRENCY_VND, 0.00004);
    }

    public static double convertToUsd(double amount, String fromCurrency) {
        Double rate = TO_USD_RATES.get(fromCurrency);
        if (rate == null) return amount;
        return amount * rate;
    }

    public static String formatCurrency(double amountUsd) {
        return String.format(java.util.Locale.US, "$%.2f", amountUsd);
    }
}
