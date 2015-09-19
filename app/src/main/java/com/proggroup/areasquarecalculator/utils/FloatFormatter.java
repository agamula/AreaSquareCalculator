package com.proggroup.areasquarecalculator.utils;

import java.util.Locale;

public class FloatFormatter {
    public static String format(float val) {
        return String.format(Locale.US, "%.4f", val);
    }
}
