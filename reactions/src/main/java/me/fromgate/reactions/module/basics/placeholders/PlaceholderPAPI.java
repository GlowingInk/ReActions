package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.placeholderapi.RaPlaceholderAPI;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

public class PlaceholderPAPI implements Placeholder.Postprocess {
    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context, @NotNull String key, @NotNull String text) {
        return RaPlaceholderAPI.processPlaceholder(context.getPlayer(), text);
    }

    @Override
    public @NotNull String getName() {
        return "placeholderapi";
    }
}
