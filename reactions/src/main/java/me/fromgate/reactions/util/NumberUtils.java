package me.fromgate.reactions.util;

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
    public static final Pattern FLOAT = Pattern.compile("-?\\d+(\\.\\d+)?");

    private NumberUtils() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static double asDouble(@Nullable String str, double def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def;
        return Double.parseDouble(str);
    }

    public static double asDouble(@Nullable String str, @NotNull DoubleSupplier def) {
        if (Utils.isStringEmpty(str) || !FLOAT.matcher(str).matches()) return def.getAsDouble();
        return Double.parseDouble(str);
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

    public static boolean isNumber(@NotNull String str) {
        return FLOAT.matcher(str).matches();
    }

    public static boolean isNumber(@NotNull String str, @NotNull Predicate<String> flag) {
        return isNumber(str) && flag.test(str);
    }

    public static boolean isNumber(@NotNull String str, @NotNull Predicate<String> flag1, @NotNull Predicate<String> flag2) {
        return isNumber(str) && flag1.test(str) && flag2.test(str);
    }

    public static boolean isNumber(@NotNull String str, @NotNull Predicate<String> flag1, @NotNull Predicate<String> flag2, @NotNull Predicate<String> flag3) {
        return isNumber(str) && flag1.test(str) && flag2.test(str) && flag3.test(str);
    }

    @SafeVarargs
    public static boolean isNumber(@NotNull String str, @NotNull Predicate<String> @NotNull ... flags) {
        return isNumber(str, Arrays.asList(flags));
    }

    public static boolean isNumber(@NotNull String str, @NotNull Iterable<? extends @NotNull Predicate<String>> flags) {
        if (!isNumber(str)) return false;
        for (Predicate<String> flag : flags) {
            if (!flag.test(str)) return false;
        }
        return true;
    }

    public static final class Is {
        private Is() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

        public static final Predicate<String> NON_ZERO = (s) -> !s.equals("0") && !s.equals("-0");
        public static final Predicate<String> POSITIVE = (s) -> !s.startsWith("-");
        public static final Predicate<String> INTEGER = (s) -> s.indexOf('.') == -1;
        public static final Predicate<String> NATURAL = NON_ZERO.and(POSITIVE).and(INTEGER);
    }

    @Deprecated
    public static boolean isInteger(@NotNull String @NotNull ... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!INT.matcher(s).matches()) return false;
        return true;
    }

    @Deprecated
    public static boolean isNumber(@NotNull String @NotNull ... str) {
        if (str.length == 0) return false;
        for (String s : str)
            if (!isNumber(s)) return false;
        return true;
    }

    @Deprecated
    public static boolean isPositiveInt(@NotNull String str) {
        return isNumber(str, Is.POSITIVE, Is.INTEGER);
    }

    @Deprecated
    public static boolean isPositiveInt(@NotNull String @NotNull ... str) {
        if (str.length == 0) return false;
        for (String s : str) {
            if (!isPositiveInt(s)) return false;
        }
        return true;
    }

    @Deprecated
    public static boolean isPositiveNonzeroInt(@NotNull String str) {
        return isNumber(str, Is.NATURAL);
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
        d *= TRIM_VALUE;
        return (d > 0 ? Math.floor(d) : Math.ceil(d)) / TRIM_VALUE;
    }
}
