package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.alias.Aliases;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@Aliases({"rnd", "rng"})
public class PlaceholderRandom implements Placeholder.Keyed {

    private static final Pattern WORD_LIST = Pattern.compile("[\\S,]*\\S");
    private static final Pattern INT_MIN_MAX = Pattern.compile("\\d+(-\\d+)?");

    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        if (NumberUtils.INT_POSITIVE.matcher(param).matches())
            return Integer.toString(Rng.nextInt(Integer.parseInt(param)));

        if (INT_MIN_MAX.matcher(param).matches())
            return Integer.toString(Rng.nextIntRanged(param));

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