package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.placeholders.resolvers.DynamicResolver;
import me.fromgate.reactions.placeholders.resolvers.KeyedResolver;
import me.fromgate.reactions.placeholders.resolvers.PreprocessResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlaceholdersManager {
    private final KeyedResolver keyed;
    private final DynamicResolver dynamic;
    private final PreprocessResolver preprocess;
    protected static int countLimit;

    public PlaceholdersManager() {
        keyed = new KeyedResolver();
        dynamic = new DynamicResolver();
        preprocess = new PreprocessResolver();
    }

    // TODO: Unstatic
    public static void setCountLimit(int countLimit) {
        PlaceholdersManager.countLimit = countLimit;
    }

    public final void registerPlaceholder(@NotNull Placeholder ph) {
        if (ph instanceof Placeholder.Dynamic phDynamic) {
            dynamic.put(phDynamic);
        } else if (ph instanceof Placeholder.Preprocess phPreprocess) {
            preprocess.put(phPreprocess);
        } else if (ph instanceof Placeholder.Keyed phKeyed) {
            if (!keyed.put(phKeyed)) {
                throw new IllegalArgumentException("Cannot register '" + ph.getName() + "' placeholder - its name is already used");
            }
        } else  {
            throw new IllegalArgumentException("Cannot register '" + ph.getName() + "' placeholder - it doesn't implement any specific Placeholder interfaces");
        }
    }

    public abstract @NotNull String parsePlaceholders(@NotNull Environment context, @NotNull String text);

    protected final @Nullable String resolvePlaceholder(@NotNull Environment context, @NotNull String phText) {
        String result = keyed.parse(context, phText);
        return result == null
                ? dynamic.parse(context, phText)
                : result;
    }

    protected final @NotNull String resolvePreprocess(@NotNull Environment context, @NotNull String fullText) {
        return preprocess.parse(context, fullText);
    }
}
