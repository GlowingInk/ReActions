package fun.reactions.util;

import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.jetbrains.annotations.NotNull;

import static ink.glowing.utils.rng.RngUtils.threadRandom;

public final class Rng {
    private Rng() {}

    public static int nextInt(int max) {
        return threadRandom().nextInt(max);
    }

    public static int nextInt(int min, int max) {
        return threadRandom().nextInt(min, max);
    }

    /**
     * Get random value by min and max values
     *
     * @param numsStr String with min-max values or just max value(e.g. "2-47", "76")
     * @return Random value
     */
    public static int nextIntRanged(@NotNull String numsStr) { // TODO Requires refactoring to nextRanged
        int index = numsStr.indexOf('-');
        if (index > -1) {
            int min = NumberUtils.parseInteger(numsStr.substring(0, index), Is.NON_NEGATIVE).orElse(0);
            int max = NumberUtils.parseInteger(numsStr.substring(index + 1), Is.NON_NEGATIVE).orElse(0);
            return max > min ? nextInt(min, max) : min;
        }
        return NumberUtils.asInteger(numsStr);
    }
}
