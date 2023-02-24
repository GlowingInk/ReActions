package fun.reactions.module.papi.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;

public class PapiPlaceholder implements Placeholder.Preprocess {
    @Override
    public @NotNull String resolveAll(@NotNull Environment env, @NotNull String text) {
        return PlaceholderAPI.setPlaceholders(env.getPlayer(), text);
    }

    @Override
    public @NotNull String getName() {
        return "placeholderapi";
    }
}
