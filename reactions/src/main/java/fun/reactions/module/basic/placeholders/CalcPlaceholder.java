package fun.reactions.module.basic.placeholders;

import fun.reactions.Cfg;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.NumberUtils;
import ink.glowing.math.ExpressionEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"calculate", "expression", "eval", "math"})
public class CalcPlaceholder implements Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        if (Cfg.modernPlaceholders || !param.contains("%")) {
            return NumberUtils.simpleFormat(ExpressionEvaluator.eval(param));
        }
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "calc";
    }
}
