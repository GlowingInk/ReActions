package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"var", "varp", "varplayer"})
public class PlaceholderVariable implements Placeholder.Keyed {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String params) {
        switch (key) {
            case "var": case "variable":
                int index = params.indexOf('.');
                if (index != -1) {
                    return ReActions.getVariables().getVariable(params.substring(0, index), params.substring(index + 1));
                } else {
                    return ReActions.getVariables().getVariable("", params);
                }

            case "varp": case "varplayer":
                return ReActions.getVariables().getVariable(context.getPlayer().getName(), params);

            default:
                return null;
        }
    }

    @Override
    public @NotNull String getName() {
        return "variable";
    }
}
