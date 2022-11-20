package me.fromgate.reactions.util;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

// TODO: In the current state it's bloated mess. Refactor
public final class NumberUtils {
    // Byte
    public static final Pattern BYTE = Pattern.compile("(2[1-5][1-6]|\\d{1,2})");
    // Integer
    public static final Pattern INT = Pattern.compile("-?\\d+");
    public static final Pattern INT_POSITIVE = Pattern.compile("\\d+");
    public static final Pattern INT_NONZERO_POSITIVE = Pattern.compile("[1-9]\\d*");
    public static Pattern INT_NONZERO = Pattern.compile("-?[1-9]\\d*");
    // Float
    public static final Pattern FLOAT = Pattern.compile("-?\\d+(\\.\\d+)?");
    public static final Pattern FLOAT_POSITIVE = Pattern.compile("\\d+(\\.\\d+)?");

    private NumberUtils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static double getDouble(String str, double def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def;
        return Double.parseDouble(str);
    }

    public static int getInteger(String str, int def) {
        if (Utils.isStringEmpty(str) || !INT.matcher(str).matches()) return def;
        return Integer.parseInt(str);
    }

    public static double getDouble(String str, DoubleSupplier def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def.getAsDouble();
        return Double.parseDouble(str);
    }

    public static int getInteger(String str, IntSupplier def) {
        if (Utils.isStringEmpty(str) || !INT.matcher(str).matches()) return def.getAsInt();
        return Integer.parseInt(str);
    }

    public static String format(double d) {
        return (d == (long) d) ?
               Long.toString((long) d) :
               Double.toString(d);
    }

    public static boolean isIntegerSigned(String... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!INT.matcher(s).matches()) return false;
        return true;
    }

    public static boolean isInteger(String str) {
        return (INT_POSITIVE.matcher(str).matches());
    }

    public static boolean isInteger(String... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!INT_POSITIVE.matcher(s).matches()) return false;
        return true;
    }

    public static boolean isNonzeroInteger(String str) {
        return INT_NONZERO_POSITIVE.matcher(str).matches();
    }

    public static boolean isNumber(String... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!FLOAT.matcher(s).matches()) return false;
        return true;
    }

    public static boolean isNumber(String str) {
        return FLOAT.matcher(str).matches();
    }

    /**
     * Check if string contains positive float
     *
     * @param numStr String to check
     * @return Is string contains positive float
     */
    public static boolean isFloat(String numStr) {
        return FLOAT_POSITIVE.matcher(numStr).matches();
    }

    /**
     * Safe transition from long to int
     *
     * @param l Long to transit
     * @return Final int
     */
    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (l > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) l;
    }

    /**
     * Trim 4+ numbers after dot
     *
     * @param d Number to trim
     * @return Trimed double
     */
    public static double trimDouble(double d) {
        return Math.round(d * 1000) / 1000d;
    }
}
