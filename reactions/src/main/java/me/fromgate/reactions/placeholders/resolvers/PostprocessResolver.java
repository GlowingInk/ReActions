package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PostprocessResolver implements Resolver {
    private final List<Placeholder.Postprocess> placeholders = new ArrayList<>();

    @Override
    public boolean put(@NotNull Placeholder ph) {
        if (ph instanceof Placeholder.Postprocess postPh) {
            placeholders.add(postPh);
            return true;
        }
        return false;
    }

    @Override
    public @NotNull String parse(@NotNull RaContext context, @NotNull String text) {
        for (Placeholder.Postprocess placeholder : placeholders) {
            text = placeholder.processPlaceholder(context, placeholder.getName(), text);
        }
        return text;
    }

    @Override
    public @NotNull Collection<Placeholder.Postprocess> getPlaceholders() {
        return placeholders;
    }
}
