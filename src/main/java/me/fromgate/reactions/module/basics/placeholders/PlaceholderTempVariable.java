package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderTempVariable implements Placeholder {
    @Override
    public @Nullable String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String text) {
        return context.getVariable(key);
    }

    @Override
    public @NotNull String getBasicName() {
        return "tempvariable";
    }
}
