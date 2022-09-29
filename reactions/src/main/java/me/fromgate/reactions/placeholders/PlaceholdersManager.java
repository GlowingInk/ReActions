package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.resolvers.EqualResolver;
import me.fromgate.reactions.placeholders.resolvers.PostprocessResolver;
import me.fromgate.reactions.placeholders.resolvers.PrefixedResolver;
import me.fromgate.reactions.placeholders.resolvers.Resolver;
import me.fromgate.reactions.placeholders.resolvers.SimpleResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PlaceholdersManager {
    private final List<Resolver<?>> processResolvers;
    private final EqualResolver equal;
    private final PrefixedResolver prefixed;
    private final SimpleResolver simple;
    private final PostprocessResolver postprocess;
    protected static int countLimit;

    public PlaceholdersManager() {
        equal = new EqualResolver();
        prefixed = new PrefixedResolver();
        simple = new SimpleResolver();
        processResolvers = List.of(equal, prefixed, simple);
        postprocess = new PostprocessResolver();
    }

    public static void setCountLimit(int countLimit) {
        PlaceholdersManager.countLimit = countLimit;
    }

    public void registerPlaceholder(@NotNull Placeholder ph) {
        boolean registered = false;
        if (ph instanceof Placeholder.Equal phEqual)        registered = equal.put(phEqual);
        if (ph instanceof Placeholder.Prefixed phPrefixed)  registered |= prefixed.put(phPrefixed);
        if (ph instanceof Placeholder.Postprocess phPost)   registered |= postprocess.put(phPost);
        if (!registered) simple.put(ph);
    }

    public final @Nullable String resolvePlaceholder(@NotNull RaContext context, @NotNull String text) {
        for (Resolver<?> resolver : processResolvers) {
            String result = resolver.parse(context, text);
            if (result != null) return result;
        }
        return null;
    }

    public final @NotNull String postprocess(@NotNull RaContext context, @NotNull String text) {
        return postprocess.parse(context, text);
    }

    public abstract String parsePlaceholders(@NotNull RaContext context, @Nullable String text);
}
