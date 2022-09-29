package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SimpleResolver implements Resolver {
    private final List<Placeholder> placeholders = new ArrayList<>();

    @Override
    public boolean put(@NotNull Placeholder ph) {
        placeholders.add(ph);
        return true;
    }

    @Override
    public @Nullable String parse(@NotNull RaContext context, @NotNull String text) {
        for (Placeholder ph : placeholders) {
            String result = ph.processPlaceholder(context, text, text);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public @NotNull Collection<Placeholder> getPlaceholders() {
        return placeholders;
    }
}
