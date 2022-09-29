package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.resolvers.EqualResolver;
import me.fromgate.reactions.placeholders.resolvers.PostprocessResolver;
import me.fromgate.reactions.placeholders.resolvers.PrefixedResolver;
import me.fromgate.reactions.placeholders.resolvers.Resolver;
import me.fromgate.reactions.placeholders.resolvers.SimpleResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaceholdersManager {
    private final List<Resolver> resolvers;
    private final PostprocessResolver postprocess;
    protected static int countLimit;

    public PlaceholdersManager() {
        resolvers = new ArrayList<>();
        resolvers.add(new EqualResolver());
        resolvers.add(new PrefixedResolver());
        resolvers.add(new SimpleResolver());
        postprocess = new PostprocessResolver();
    }

    public static void setCountLimit(int countLimit) {
        PlaceholdersManager.countLimit = countLimit;
    }

    public void registerPlaceholder(@NotNull Placeholder ph) {
        if (ph instanceof Placeholder.Postprocess) {
            postprocess.put(ph);
        } else for (Resolver resolver : resolvers) {
            if (resolver.put(ph)) break;
        }
    }

    public final @Nullable String resolvePlaceholder(@NotNull RaContext context, @NotNull String text) {
        for (Resolver resolver : resolvers) {
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
