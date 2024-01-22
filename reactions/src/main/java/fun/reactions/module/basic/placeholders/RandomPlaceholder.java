package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.Rng;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;
import java.util.regex.Pattern;

@Aliased.Names({"rnd", "rng"})
public class RandomPlaceholder implements Placeholder {

    private static final Pattern WORD_LIST = Pattern.compile("[\\S,]*\\S");
    private static final Pattern INT_MIN_MAX = Pattern.compile("\\d+(-\\d+)?");

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        if (param.isEmpty()) return null;

        OptionalInt rangeOpt = NumberUtils.parseInteger(param, Is.NON_NEGATIVE);
        if (rangeOpt.isPresent()) {
            return Integer.toString(Rng.nextInt(rangeOpt.getAsInt()));
        }

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
