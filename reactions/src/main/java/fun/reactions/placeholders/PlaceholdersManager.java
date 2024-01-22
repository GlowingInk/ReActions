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
            throw new IllegalArgumentException("Cannot register '" + ph.getName() + "' placeholder - it doesn't implement any specific Placeholder interfaces");
        }
    }

    public abstract @NotNull String parse(@NotNull Environment env, @NotNull String text);

    protected final @Nullable String resolvePlaceholder(@NotNull Environment env, @NotNull String phText) {
        return resolver.resolve(env, phText);
    }
}
