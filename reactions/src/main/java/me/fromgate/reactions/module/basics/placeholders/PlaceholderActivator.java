package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.alias.Aliases;
import org.jetbrains.annotations.NotNull;

@Aliases("activatorname")
public class PlaceholderActivator implements Placeholder {
    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        return context.getActivatorName();
    }

    @Override
    public @NotNull String getName() {
        return "activator_name";
    }
}
