package fun.reactions.module.papi.placeholders;

import fun.reactions.Cfg;
import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PapiPlaceholder implements Placeholder.Dynamic {
    private final PlaceholderAPIPlugin papiPlugin;

    public PapiPlaceholder(PlaceholderAPIPlugin papiPlugin) {
        this.papiPlugin = papiPlugin;
    }

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
        String[] split = phText.split(Cfg.papiSplitter, 2);
        var phExpansion = papiPlugin.getLocalExpansionManager().getExpansion(split[0]);
        return phExpansion == null
                ? null
                : phExpansion.onRequest(env.getPlayer(), split.length > 1 ? split[1] : "");
    }

    @Override
    public @NotNull String getName() {
        return "placeholderapi";
    }
}
