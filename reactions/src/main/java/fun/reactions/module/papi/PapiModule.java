package fun.reactions.module.papi;

import fun.reactions.ReActions;
import fun.reactions.module.Module;
import fun.reactions.module.papi.external.RaPapiExpansion;
import fun.reactions.module.papi.placeholders.PapiPlaceholder;
import fun.reactions.placeholders.Placeholder;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PapiModule implements Module { // TODO To own jar
    private PlaceholderAPIPlugin papiPlugin;

    @Override
    public @NotNull Collection<String> requiredPlugins() {
        return List.of("PlaceholderAPI");
    }

    @Override
    public void preRegister(@NotNull ReActions.Platform platform) {
        new RaPapiExpansion().register();
        this.papiPlugin = (PlaceholderAPIPlugin) Objects.requireNonNull(
                platform.getServer().getPluginManager().getPlugin("PlaceholderAPI")
        );
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "imDaniX");
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders() {
        return List.of(new PapiPlaceholder(papiPlugin));
    }

    @Override
    public @NotNull String getName() {
        return "PlaceholderAPI";
    }
}
