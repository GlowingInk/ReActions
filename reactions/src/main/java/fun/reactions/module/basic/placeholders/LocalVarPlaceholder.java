package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocalVarPlaceholder implements Placeholder.Dynamic {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
        return env.getVariables().getStringUnsafe(phText);
    }

    @Override
    public @NotNull String getName() {
        return "local_variable";
    }
}