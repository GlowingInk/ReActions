package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderTempVariable implements Placeholder.Dynamic {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String phText) {
        return context.getVariable(phText);
    }

    @Override
    public @NotNull String getName() {
        return "temp_variable";
    }
}
