package me.fromgate.reactions.util.math;

import lombok.experimental.UtilityClass;
import me.fromgate.reactions.util.Utils;

import java.util.regex.Pattern;

@UtilityClass
public class NumberUtils {
    // Byte
    public Pattern BYTE = Pattern.compile("(2[1-5][1-6]|\\d{1,2})");
    // Integer
    public Pattern INT_POSITIVE = Pattern.compile("\\d+");
    public Pattern INT = Pattern.compile("-?\\d+");
    public Pattern INT_NONZERO_POSITIVE = Pattern.compile("[1-9]\\d*");
    public Pattern INT_NONZERO = Pattern.compile("-?[1-9]\\d*");
    // Float
    public Pattern FLOAT_POSITIVE = Pattern.compile("\\d+(\\.\\d+)?");
    public Pattern FLOAT = Pattern.compile("-?\\d+(\\.\\d+)?");
    public Pattern FLOAT_WITHZERO = Pattern.compile("^\\d+\\.0$");

    public double getDouble(String str, double def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def;
        return Double.parseDouble(str);
    }

    public int getInteger(String str, int def) {
        if (Utils.isStringEmpty(str) || !INT.matcher(str).matches()) return def;
        return Integer.parseInt(str);
    }

    public String format(double d) {
        return (d == (long) d) ?
               Long.toString((long) d) :
               Double.toString(d);
    }

    // TODO: Should be removed or refactored
    public boolean isIntegerSigned(String... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!INT.matcher(s).matches()) return false;
        return true;
    }

    public boolean isInteger(String str) {
        return (INT_POSITIVE.matcher(str).matches());
    }

    public boolean isInteger(String... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!INT_POSITIVE.matcher(s).matches()) return false;
        return true;
    }

    public boolean isNonzeroInteger(String str) {
        return INT_NONZERO_POSITIVE.matcher(str).matches();
    }

    public boolean isNonzeroInteger(String... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!INT_NONZERO_POSITIVE.matcher(s).matches()) return false;
        return true;
    }

    public boolean isNumber(String... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!FLOAT.matcher(s).matches()) return false;
        return true;
    }

    /**
     * Check if string contains positive float
     *
     * @param numStr String to check
     * @return Is string contains positive float
     */
    public boolean isFloat(String numStr) {
        return FLOAT_POSITIVE.matcher(numStr).matches();
    }

    /**
     * Safe transition from long to int
     *
     * @param l Long to transit
     * @return Final int
     */
    public int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (l > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) l;
    }

    /**
     * Get array of integers sorted by value
     *
     * @param arg1 First integer
     * @param arg2 Second integer
     * @return Array of integers, where first value is minimal
     */
    public int[] sortedIntPair(int arg1, int arg2) {
        int[] pair = new int[2];
        if (arg1 > arg2) {
            pair[0] = arg2;
            pair[1] = arg1;
        } else {
            pair[0] = arg1;
            pair[1] = arg2;
        }
        return pair;
    }

    /**
     * Trim 4+ numbers after dot
     *
     * @param d Number to trim
     * @return Trimed double
     */
    public double trimDouble(double d) {
        return Math.round(d * 1000) / 1000d;
    }
}
