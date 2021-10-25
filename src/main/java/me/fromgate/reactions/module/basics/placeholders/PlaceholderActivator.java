package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;

@Alias("activatorname")
public class PlaceholderActivator implements Placeholder.Equal {
    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String param) {
        return context.getActivatorName();
    }

    @Override
    public @NotNull String getId() {
        return "activator_name";
    }
}
