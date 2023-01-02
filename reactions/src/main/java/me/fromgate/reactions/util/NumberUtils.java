package me.fromgate.reactions.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

// TODO: In the current state it's bloated mess. Refactor
public final class NumberUtils {

    // Integer
    public static final Pattern INT = Pattern.compile("-?\\d+");
    public static final Pattern INT_POSITIVE = Pattern.compile("\\d+");
    public static final Pattern INT_NONZERO_POSITIVE = Pattern.compile("[1-9]\\d*");
    public static Pattern INT_NONZERO = Pattern.compile("-?[1-9]\\d*");
    // Float
    public static final Pattern FLOAT = Pattern.compile("-?\\d+(\\.\\d+)?");
    public static final Pattern FLOAT_POSITIVE = Pattern.compile("\\d+(\\.\\d+)?");

    private NumberUtils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static double asDouble(@Nullable String str, double def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def;
        return Double.parseDouble(str);
    }

    public static double asDouble(@Nullable String str, DoubleSupplier def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def.getAsDouble();
        return Double.parseDouble(str);
    }

    public static int asInteger(@Nullable String str, int def) {
        if (Utils.isStringEmpty(str) || !INT.matcher(str).matches()) return def;
        return Integer.parseInt(str);
    }

    public static int asInteger(@Nullable String str, IntSupplier def) {
        if (Utils.isStringEmpty(str) || !INT.matcher(str).matches()) return def.getAsInt();
        return Integer.parseInt(str);
    }

    public static @NotNull String format(double d) {
        return d == (long) d
                ? Long.toString((long) d)
                : Double.toString(d);
    }

    public static boolean isInteger(@NotNull String @NotNull ... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!INT.matcher(s).matches()) return false;
        return true;
    }

    public static boolean isPositiveInt(@NotNull String str) {
        return (INT_POSITIVE.matcher(str).matches());
    }

    public static boolean isPositiveInt(@NotNull String @NotNull ... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!isPositiveInt(s)) return false;
        return true;
    }

    public static boolean isPositiveNonzeroInt(@NotNull String str) {
        return INT_NONZERO_POSITIVE.matcher(str).matches();
    }

    public static boolean isNumber(@NotNull String @NotNull ... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!isNumber(s)) return false;
        return true;
    }

    public static boolean isNumber(@NotNull String str) {
        return FLOAT.matcher(str).matches();
    }

    /**
     * Check if string contains positive float
     *
     * @param numStr String to check
     * @return Is string contains positive float
     */
    public static boolean isPositive(@NotNull String numStr) {
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
     * @return Trimmed double
     */
    public static double trimDouble(double d) {
        return Math.round(d * 1000) / 1000d;
    }
}
