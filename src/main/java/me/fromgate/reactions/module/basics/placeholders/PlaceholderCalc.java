package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.MathEvaluator;
import me.fromgate.reactions.util.math.NumberUtils;

@Alias({"calculate", "expression", "eval"})
public class PlaceholderCalc implements Placeholder.Prefixed {
    @Override
    public String processPlaceholder(RaContext context, String key, String param) {
        if(!param.contains("%")) try {
            return NumberUtils.format(MathEvaluator.eval(param));
        } catch (NumberFormatException | ArithmeticException ignore) {
            // TODO: Error
        }
        return null;
    }

    @Override
    public String getPrefix() {
        return "calc";
    }
}
