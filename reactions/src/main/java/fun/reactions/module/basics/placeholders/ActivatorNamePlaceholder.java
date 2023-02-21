package fun.reactions.module.basics.placeholders;

import fun.reactions.logic.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("activatorname")
public class ActivatorNamePlaceholder implements Placeholder.Keyed {
    @Override
    public @NotNull String resolve(@NotNull Environment env, @NotNull String key, @NotNull String param) {
        return env.getActivatorName();
    }

    @Override
    public @NotNull String getName() {
        return "activator_name";
    }
}
