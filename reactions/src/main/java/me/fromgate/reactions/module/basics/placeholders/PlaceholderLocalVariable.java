package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderLocalVariable implements Placeholder.Dynamic {
    @Override
    public @Nullable String processPlaceholder(@NotNull Environment context, @NotNull String phText) {
        return context.getVariables().getUnsafeString(phText);
    }

    @Override
    public @NotNull String getName() {
        return "temp_variable";
    }
}
