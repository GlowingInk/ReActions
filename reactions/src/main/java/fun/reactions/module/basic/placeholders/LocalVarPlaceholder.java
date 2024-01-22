package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"local", "local_var", "context"})
public class LocalVarPlaceholder implements Placeholder.Dynamic, Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
        return env.getVariables().getStringUnsafe(phText);
    }

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        return resolve(env, params);
    }

    @Override
    public @NotNull String getName() {
        return "local_variable";
    }
}
