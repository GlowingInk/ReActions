package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.NumberUtils.Is;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@Aliased.Names({"rnd", "rng"})
public class RandomPlaceholder implements Placeholder.Keyed {

    private static final Pattern WORD_LIST = Pattern.compile("[\\S,]*\\S");
    private static final Pattern INT_MIN_MAX = Pattern.compile("\\d+(-\\d+)?");

    @Override
    public @NotNull String processPlaceholder(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        if (NumberUtils.isNumber(param, Is.NATURAL))
            return Integer.toString(Rng.nextInt(Integer.parseInt(param)));

        if (INT_MIN_MAX.matcher(param).matches()) {
            return Integer.toString(Rng.nextIntRanged(param));
        }

        if (WORD_LIST.matcher(param).matches()) {
            String[] ln = param.split(",");
            if (ln.length == 0) return param;
            return ln[Rng.nextInt(ln.length)];
        }
        return param;
    }

    @Override
    public @NotNull String getName() {
        return "random";
    }
}
