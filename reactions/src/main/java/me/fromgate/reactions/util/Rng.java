package me.fromgate.reactions.util;

import me.fromgate.reactions.util.NumberUtils.Is;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Rng {
    private Rng() {}

    public static <T> T randomElement(List<T> list) {
        return list.get(Rng.nextInt(list.size()));
    }

    public static boolean percentChance(double chance) {
        return Rng.nextDouble(100) < chance;
    }

    public static boolean chance(double chance) {
        return nextDouble() < chance;
    }

    public static boolean nextBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static int nextInt(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    public static int nextInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static double nextDouble(double max) {
        return ThreadLocalRandom.current().nextDouble(max);
    }

    public static double nextDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
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
            int min = 0;
            String minStr = numsStr.substring(0, index);
            if (NumberUtils.isNumber(minStr, Is.NATURAL)) {
                min = Integer.parseInt(minStr);
            }
            int max = 0;
            String maxStr = numsStr.substring(index + 1);
            if (NumberUtils.isNumber(maxStr, Is.NATURAL)) {
                max = Integer.parseInt(maxStr) + 1;
            }
            return max > min ? nextInt(min, max) : min;
        }
        return NumberUtils.asInteger(numsStr);
    }
}
