package fun.reactions.util.num;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoublePredicate;

/**
 * A collection of useful number predicates
 */
public final class Is {
    private Is() {}

    /**
     * Checks if number is an integer (has no floating value)
     */
    public static final DoublePredicate INTEGER = v -> v == (long) v;

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

    /**
     * Checks if number is positive and integer
     */
    public static final DoublePredicate POSITIVE_NATURAL = POSITIVE.and(INTEGER);
    /**
     * Checks if number is positive or zero and integer
     */
    public static final DoublePredicate NATURAL = NON_NEGATIVE.and(INTEGER);

    public static @NotNull DoublePredicate inRange(double min, double max) {
        return v -> min <= v && v < max;
    }
}
