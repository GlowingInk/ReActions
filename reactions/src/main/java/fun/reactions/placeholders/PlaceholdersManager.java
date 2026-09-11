package fun.reactions.placeholders;

import fun.reactions.cfg.RaConfiguration;
import fun.reactions.cfg.Reloadable;
import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlaceholdersManager implements Reloadable {
    private final PlaceholderResolver resolver;

    protected int countLimit;

    public PlaceholdersManager() {
        resolver = new PlaceholderResolver();
    }

    @Override
    public void acceptReload(@NotNull RaConfiguration config) {
        countLimit = config.generalCfg().placeholders().limit();
    }

    public final void registerPlaceholder(@NotNull Placeholder ph) {
        if (!resolver.add(ph)) {
            throw new IllegalStateException("Cannot register " + ph.getClass() + " placeholder - " +
                    "the name '" + ph.getName() + "' is already registered");
        }
    }

    public abstract @NotNull String parse(@NotNull Environment env, @NotNull String text);

    public final @Nullable String resolvePlaceholder(@NotNull Environment env, @NotNull String phText) {
        return resolver.resolve(env, phText);
    }
}
