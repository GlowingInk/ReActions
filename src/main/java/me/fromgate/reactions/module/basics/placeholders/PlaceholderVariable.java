package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;

@Alias({"varp", "variable"})
public class PlaceholderVariable implements Placeholder.Prefixed {
    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String prefix, @NotNull String text) {
        switch (prefix) {
            case "var":
            case "variable":
                String[] varSplit = text.split("\\.", 2);
                if (varSplit.length > 1) {
                    return ReActions.getVariables().getVariable(varSplit[0], varSplit[1]);
                } else {
                    return ReActions.getVariables().getVariable("", varSplit[0]);
                }

            case "varp":
                return ReActions.getVariables().getVariable(context.getPlayer().getName(), text);

            default:
                return null;
        }
    }

    @Override
    public @NotNull String getPrefix() {
        return "var";
    }
}
