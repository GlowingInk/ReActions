package fun.reactions.module.basics.placeholders;

import fun.reactions.externals.placeholderapi.RaPlaceholderAPI;
import fun.reactions.logic.environment.Environment;
import fun.reactions.placeholders.Placeholder;
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
