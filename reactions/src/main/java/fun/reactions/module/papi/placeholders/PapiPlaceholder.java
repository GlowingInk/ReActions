package fun.reactions.module.papi.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names("papi")
public class PapiPlaceholder implements Placeholder.Dynamic, Placeholder {
    private final PlaceholderAPIPlugin papiPlugin;

    public PapiPlaceholder(PlaceholderAPIPlugin papiPlugin) {
        this.papiPlugin = papiPlugin;
    }

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
        String[] split = phText.split("_", 2);
        var phExpansion = papiPlugin.getLocalExpansionManager().getExpansion(split[0]);
        return phExpansion == null
                ? null
                : phExpansion.onRequest(env.getPlayer(), split.length > 1 ? split[1] : "");
    }

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        return resolve(env, params);
    }

    @Override
    public @NotNull String getName() {
        return "placeholderapi";
    }
}
