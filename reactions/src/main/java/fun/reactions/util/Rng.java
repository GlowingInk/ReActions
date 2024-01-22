package fun.reactions.util;

import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class Rng {
    private Rng() {}

    public static <T> T randomElement(List<T> list) {
        return list.get(Rng.nextInt(list.size()));
    }

    public static boolean percentChance(double chance) {
        return nextDouble(100) < chance;
    }

    public static boolean chance(double chance) {
        return nextDouble() < chance;
    }

    public static boolean nextBoolean() {
        return threadRandom().nextBoolean();
    }

    public static int nextInt(int max) {
        return threadRandom().nextInt(max);
    }

    public static int nextInt(int min, int max) {
        return threadRandom().nextInt(min, max);
    }

    public static double nextDouble() {
        return threadRandom().nextDouble();
    }

    public static double nextDouble(double max) {
        return threadRandom().nextDouble(max);
    }

    public static double nextDouble(double min, double max) {
        return threadRandom().nextDouble(min, max);
    }

    /**
     * Get random value by min and max values
     *
     * @param numsStr String with min-max values or just max value(e.g. "2-47", "76")
     * @return Random value
     */
    public static int nextIntRanged(String numsStr) { // TODO Requires refactoring to nextRanged
        int index = numsStr.indexOf('-');
        if (index > -1) {
            int min = NumberUtils.parseInteger(numsStr.substring(0, index), Is.NATURAL).orElse(0);
            int max = NumberUtils.parseInteger(numsStr.substring(index + 1), Is.NATURAL).orElse(0);
            return max > min ? nextInt(min, max) : min;
        }
        return NumberUtils.asInteger(numsStr);
    }

    public static @NotNull RandomGenerator threadRandom() {
        return ThreadLocalRandom.current();
    }
}
