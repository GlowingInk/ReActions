package fun.reactions.util.num;

import fun.reactions.util.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.*;
import java.util.regex.Pattern;

public final class NumberUtils {
    private static final double TRIM_VALUE = 1_0000;

    public static final Pattern INTEGER = Pattern.compile("-?\\d+");
    public static final Predicate<String> IS_INTEGER = INTEGER.asMatchPredicate();
    public static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+([eE][+\\-]\\d+)?)?");
    public static final Predicate<String> IS_NUMBER = NUMBER.asMatchPredicate();

    private NumberUtils() {}

    public static boolean equals(double a, double b, double error) {
        return Math.abs(a - b) < error;
    }

    public static double asDouble(@Nullable String str) {
        return asDouble(str, 0);
    }

    public static double asDouble(@Nullable String str, double def) {
        return isNumber(str)
                ? Double.parseDouble(str)
                : def;
    }

    public static double asDouble(@Nullable String str, @NotNull DoubleSupplier def) {
        return isNumber(str)
                ? Double.parseDouble(str)
                : def.getAsDouble();
    }

    public static @NotNull OptionalDouble parseDouble(@NotNull String str) {
        return IS_NUMBER.test(str)
                ? OptionalDouble.of(Double.parseDouble(str))
                : OptionalDouble.empty();
    }

    public static @NotNull OptionalDouble parseDouble(@NotNull String str, @NotNull DoublePredicate predicate) {
        if (IS_NUMBER.test(str)) {
            double d = Double.parseDouble(str);
            if (predicate.test(d)) return OptionalDouble.of(d);
        }
        return OptionalDouble.empty();
    }

    public static int asInteger(@Nullable String str) {
        return asInteger(str, 0);
    }

    public static int asInteger(@Nullable String str, int def) {
        return isInteger(str)
                ? Integer.parseInt(str)
                : def;
    }

    public static int asInteger(@Nullable String str, @NotNull IntSupplier def) {
        return isInteger(str)
                ? Integer.parseInt(str)
                : def.getAsInt();
    }

    public static @NotNull OptionalInt parseInteger(@NotNull String str) {
        return IS_INTEGER.test(str)
                ? OptionalInt.of(Integer.parseInt(str))
                : OptionalInt.empty();
    }

    public static @NotNull OptionalInt parseInteger(@NotNull String str, @NotNull IntPredicate predicate) {
        if (IS_INTEGER.test(str)) {
            int v = Integer.parseInt(str);
            if (predicate.test(v)) return OptionalInt.of(v);
        }
        return OptionalInt.empty();
    }

    public static @NotNull OptionalInt parseInteger(@NotNull String str, @NotNull DoublePredicate predicate) {
        if (IS_INTEGER.test(str)) {
            int v = Integer.parseInt(str);
            if (predicate.test(v)) return OptionalInt.of(v);
        }
        return OptionalInt.empty();
    }

    @Contract("null -> false")
    public static boolean isNumber(@Nullable String str) {
        return !Utils.isStringEmpty(str) && IS_NUMBER.test(str);
    }

    @Contract("null -> false")
    public static boolean isInteger(@Nullable String str) {
        return !Utils.isStringEmpty(str) && IS_INTEGER.test(str);
    }

    /**
     * Safe transition from long to int
     *
     * @param l Long to transit
     * @return Final int
     */
    public static int compactLong(long l) {
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
        return Math.floor(d * TRIM_VALUE) / TRIM_VALUE;
    }

    public static @NotNull String simpleFormat(double d) {
        return d == (long) d
                ? Long.toString((long) d)
                : Double.toString(d);
    }
}
