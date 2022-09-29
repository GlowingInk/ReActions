package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.resolvers.DynamicResolver;
import me.fromgate.reactions.placeholders.resolvers.KeyedResolver;
import me.fromgate.reactions.placeholders.resolvers.PostprocessResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlaceholdersManager {
    private final KeyedResolver keyed;
    private final DynamicResolver dynamic;
    private final PostprocessResolver postprocess;
    protected static int countLimit;

    public PlaceholdersManager() {
        keyed = new KeyedResolver();
        dynamic = new DynamicResolver();
        postprocess = new PostprocessResolver();
    }

    // TODO Unstatic
    public static void setCountLimit(int countLimit) {
        PlaceholdersManager.countLimit = countLimit;
    }

    public final void registerPlaceholder(@NotNull Placeholder ph) {
        if (ph instanceof Placeholder.Dynamic phDynamic) {
            dynamic.put(phDynamic);
        } else if (ph instanceof Placeholder.Postprocess phPost) {
            postprocess.put(phPost);
        } else if (!keyed.put(ph)) {
            throw new IllegalArgumentException("Cannot register '" + ph.getName() + "' placeholder. It isn't Dynamic nor Postprocess, and it's key is already used");
        }
    }

    public final @Nullable String resolvePlaceholder(@NotNull RaContext context, @NotNull String phText) {
        String result = keyed.parse(context, phText);
        return result == null
                ? dynamic.parse(context, phText)
                : result;
    }

    public final @NotNull String resolvePostprocess(@NotNull RaContext context, @NotNull String fullText) {
        return postprocess.parse(context, fullText);
    }

    public abstract String parsePlaceholders(@NotNull RaContext context, @Nullable String text);
}
