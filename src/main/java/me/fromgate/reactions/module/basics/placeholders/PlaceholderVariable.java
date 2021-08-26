package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;

@Alias({"varp", "variable"})
public class PlaceholderVariable implements Placeholder.Prefixed {
    @Override
    public String processPlaceholder(RaContext context, String prefix, String text) {
        switch (prefix) {
            case "var":
            case "variable":
                String[] varSplit = text.split("\\.", 2);
                if(varSplit.length > 1) {
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
    public String getPrefix() {
        return "var";
    }
}
