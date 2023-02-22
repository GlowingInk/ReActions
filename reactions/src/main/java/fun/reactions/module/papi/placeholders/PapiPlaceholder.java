package fun.reactions.module.papi.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.module.papi.external.RaPlaceholderApi;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

public class PapiPlaceholder implements Placeholder.Preprocess {
    @Override
    public @NotNull String resolveAll(@NotNull Environment env, @NotNull String text) {
        return RaPlaceholderApi.processPlaceholder(env.getPlayer(), text);
    }

    @Override
    public @NotNull String getName() {
        return "placeholderapi";
    }
}
