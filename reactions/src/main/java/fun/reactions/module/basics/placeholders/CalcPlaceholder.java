package fun.reactions.module.basics.placeholders;

import fun.reactions.Cfg;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.naming.Aliased;
import ink.glowing.math.ExpressionEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"calculate", "expression", "eval", "math"})
public class CalcPlaceholder implements Placeholder.Keyed {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        if (Cfg.modernPlaceholders || !param.contains("%")) {
            return NumberUtils.format(ExpressionEvaluator.eval(param));
        }
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "calc";
    }
}
