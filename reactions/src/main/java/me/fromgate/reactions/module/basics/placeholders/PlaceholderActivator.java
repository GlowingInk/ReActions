package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("activatorname")
public class PlaceholderActivator implements Placeholder.Keyed {
    @Override
    public @NotNull String processPlaceholder(@NotNull Environment context, @NotNull String key, @NotNull String param) {
        return context.getActivatorName();
    }

    @Override
    public @NotNull String getName() {
        return "activator_name";
    }
}
