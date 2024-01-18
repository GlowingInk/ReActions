package fun.reactions.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class NumberUtils {
    private static final double TRIM_VALUE = 1_0000;

    public static final Pattern INT = Pattern.compile("-?\\d+");
    public static final Pattern FLOAT = Pattern.compile("-?\\d+(?:\\.\\d+([eE][+\\-]\\d+)?)?");

    private NumberUtils() {}

    public static boolean equals(double a, double b, double error) {
        return Math.abs(a - b) < error;
    }

    public static double asDouble(@Nullable String str) {
        return asDouble(str, 0);
    }

    public static double asDouble(@Nullable String str, double def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def;
        return Double.parseDouble(str);
    }

    public static double asDouble(@Nullable String str, @NotNull DoubleSupplier def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def.getAsDouble();
        return Double.parseDouble(str);
    }

    public static int asInteger(@Nullable String str) {
        return asInteger(str, 0);
    }

    public static int asInteger(@Nullable String str, int def) {
        if (Utils.isStringEmpty(str) || !INT.matcher(str).matches()) return def;
        return Integer.parseInt(str);
    }

    public static int asInteger(@Nullable String str, @NotNull IntSupplier def) {
        if (Utils.isStringEmpty(str) || !INT.matcher(str).matches()) return def.getAsInt();
        return Integer.parseInt(str);
    }

    public static @NotNull String format(double d) {
        return d == (long) d
                ? Long.toString((long) d)
                : Double.toString(d);
    }

    @Contract("null -> false")
    public static boolean isNumber(@Nullable String str) {
        return str != null && FLOAT.matcher(str).matches();
    }

    @Contract("null, _ -> false")
    public static boolean isNumber(@Nullable String str, @NotNull Predicate<String> flag) {
        return isNumber(str) && flag.test(str);
    }

    @SafeVarargs
    @Contract("null, _ -> false")
    public static boolean isNumber(@Nullable String str, @NotNull Predicate<String> @NotNull ... flags) {
        return isNumber(str, Arrays.asList(flags));
    }

    @Contract("null, _ -> false")
    public static boolean isNumber(@Nullable String str, @NotNull Iterable<? extends @NotNull Predicate<String>> flags) {
        if (!isNumber(str)) return false;
        for (Predicate<String> flag : flags) {
            if (!flag.test(str)) return false;
        }
        return true;
    }

    // TODO That's, actually, a very bad solution...
    public static final class Is {
        private Is() {}

        public static final Predicate<String> NON_ZERO = Pattern.compile("-?0+(?:\\.0+)?").asMatchPredicate().negate();
        public static final Predicate<String> POSITIVE = (s) -> !s.startsWith("-");
        public static final Predicate<String> INTEGER = (s) -> s.indexOf('.') == -1;

        public static final Predicate<String> NATURAL = POSITIVE.and(INTEGER);
        public static final Predicate<String> POSITIVE_NATURAL = NATURAL.and(NON_ZERO);
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
}
