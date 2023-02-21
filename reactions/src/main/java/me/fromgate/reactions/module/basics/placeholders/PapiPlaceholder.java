package me.fromgate.reactions.module.basics.placeholders;

import me.fromgate.reactions.externals.placeholderapi.RaPlaceholderAPI;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

public class PapiPlaceholder implements Placeholder.Preprocess {
    @Override
    public @NotNull String processPlaceholder(@NotNull Environment env, @NotNull String text) {
        return text.contains("%")
                ? RaPlaceholderAPI.processPlaceholder(env.getPlayer(), text)
                : text;
    }

    @Override
    public @NotNull String getName() {
        return "placeholderapi";
    }
}
