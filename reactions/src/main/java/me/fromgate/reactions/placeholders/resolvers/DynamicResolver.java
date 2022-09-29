package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class DynamicResolver implements Resolver<Placeholder.Dynamic> {
    private final List<Placeholder.Dynamic> placeholders = new ArrayList<>();

    @Override
    public boolean put(@NotNull Placeholder.Dynamic ph) {
        placeholders.add(ph);
        return true;
    }

    @Override
    public @Nullable String parse(@NotNull RaContext context, @NotNull String phText) {
        for (Placeholder.Dynamic ph : placeholders) {
            String result = ph.processPlaceholder(context, ph.getName(), phText);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public @NotNull Collection<Placeholder.Dynamic> getPlaceholders() {
        return new HashSet<>(placeholders);
    }
}
