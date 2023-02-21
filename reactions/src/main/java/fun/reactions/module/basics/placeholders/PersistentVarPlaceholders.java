package fun.reactions.module.basics.placeholders;

import fun.reactions.ReActions;
import fun.reactions.logic.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"var", "varp", "varplayer"})
public class PersistentVarPlaceholders implements Placeholder.Keyed {
    @Override
    public @Nullable String processPlaceholder(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        switch (key) {
            case "var": case "variable":
                int index = params.indexOf('.');
                if (index != -1) {
                    return ReActions.getVariables().getVariable(params.substring(0, index), params.substring(index + 1));
                } else {
                    return ReActions.getVariables().getVariable("", params);
                }

            case "varp": case "varplayer":
                return env.getPlayer() == null ? ReActions.getVariables().getVariable(env.getPlayer().getName(), params) : null;

            default:
                return null;
        }
    }

    @Override
    public @NotNull String getName() {
        return "variable";
    }
}
