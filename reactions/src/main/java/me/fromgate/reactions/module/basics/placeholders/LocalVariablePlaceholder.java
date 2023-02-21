package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocalVariablePlaceholder implements Placeholder.Dynamic {
    @Override
    public @Nullable String processPlaceholder(@NotNull Environment env, @NotNull String phText) {
        return env.getVariables().getStringUnsafe(phText);
    }

    @Override
    public @NotNull String getName() {
        return "local_variable";
    }
}
