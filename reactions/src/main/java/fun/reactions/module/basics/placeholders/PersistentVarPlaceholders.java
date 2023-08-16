package fun.reactions.module.basics.placeholders;

import fun.reactions.PersistentVariablesManager;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"var", "varp", "varplayer"})
public class PersistentVarPlaceholders implements Placeholder.Keyed {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        PersistentVariablesManager variablesManager = env.getPlatform().getPersistentVariables();
        switch (key) {
            case "var": case "variable":
                int index = params.indexOf('.');
                if (index != -1) {
                    return variablesManager.getVariable(params.substring(0, index), params.substring(index + 1));
                } else {
                    return variablesManager.getVariable("", params);
                }

            case "varp": case "varplayer":
                return env.getPlayer() != null
                        ? variablesManager.getVariable(env.getPlayer().getName(), params)
                        : null;

            default:
                return null;
        }
    }

    @Override
    public @NotNull String getName() {
        return "variable";
    }
}
