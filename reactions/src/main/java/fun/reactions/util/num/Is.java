package fun.reactions.util.num;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoublePredicate;

/**
 * A collection of useful number predicates
 */
public final class Is {
    private Is() {}

    /**
     * Checks if number is positive
     */
    public static final DoublePredicate POSITIVE = v -> v > 0;
    /**
     * Check if number is positive or zero
     */
    public static final DoublePredicate NON_NEGATIVE = v -> v >= 0;
    /**
     * Checks if number is negative
     */
    public static final DoublePredicate NEGATIVE = v -> v < 0;
    /**
     * Checks if number is negative or zero
     */
    public static final DoublePredicate NON_POSITIVE = v -> v <= 0;

    public static @NotNull DoublePredicate inRange(double min, double max) {
        return v -> min <= v && v < max;
    }
}
