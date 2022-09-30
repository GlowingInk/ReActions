package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.alias.Aliases;
import me.imdanix.math.ExpressionEvaluator;
import me.imdanix.math.MathDictionary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliases({"calculate", "expression", "eval"})
public class PlaceholderCalc implements Placeholder.Keyed {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        if (Cfg.modernPlaceholders || !param.contains("%")) try {
            return NumberUtils.format(ExpressionEvaluator.eval(param, MathDictionary.INSTANCE));
        } catch (NumberFormatException | ArithmeticException ignore) {
            // TODO: Error
        }
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "calc";
    }
}
