package fun.reactions.module.papi;

import fun.reactions.ReActions;
import fun.reactions.module.Module;
import fun.reactions.module.papi.external.RaPlaceholderApi;
import fun.reactions.module.papi.placeholders.PapiPlaceholder;
import fun.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class PapiModule implements Module {
    @Override
    public @NotNull Collection<String> requiredPlugins() {
        return List.of("PlaceholderAPI");
    }

    @Override
    public void preRegister(ReActions.@NotNull Platform platform) {
        RaPlaceholderApi.init();
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return List.of("fromgate", "MaxDikiy", "imDaniX");
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders() {
        return List.of(new PapiPlaceholder());
    }

    @Override
    public @NotNull String getName() {
        return "PlaceholderAPI";
    }
}
