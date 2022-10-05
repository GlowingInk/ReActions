package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.placeholderapi.RaPlaceholderAPI;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

public class PlaceholderPAPI implements Placeholder.Preprocess {
    @Override
    public @NotNull String processPlaceholder(@NotNull RaContext context,@NotNull String text) {
        return text.contains("%")
                ? RaPlaceholderAPI.processPlaceholder(context.getPlayer(), text)
                : text;
    }

    @Override
    public @NotNull String getName() {
        return "placeholderapi";
    }
}
