package fun.reactions.placeholders;

import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlaceholdersManager {
    private final PlaceholderResolver resolver;

    protected static int countLimit;

    public PlaceholdersManager() {
        resolver = new PlaceholderResolver();
    }

    // TODO: Unstatic
    @ApiStatus.Internal
    public static void setCountLimit(int countLimit) {
        PlaceholdersManager.countLimit = countLimit;
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
