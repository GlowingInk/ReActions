package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class EqualResolver implements Resolver {
    private final Map<String, Placeholder.Equal> placeholders = new HashMap<>();

    @Override
    public boolean put(@NotNull Placeholder ph) {
        if (ph instanceof Placeholder.Equal equalPh) {
            String id = ph.getName().toLowerCase(Locale.ROOT);
            if (placeholders.containsKey(id)) return false;
            placeholders.put(id, equalPh);
            for (String alias : Utils.getAliases(ph))
                placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), equalPh);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable String parse(@NotNull RaContext context, @NotNull String text) {
        String key = text.toLowerCase(Locale.ROOT);
        Placeholder ph = placeholders.get(key);
        if (ph == null) return null;
        return ph.processPlaceholder(context, key, text);
    }

    @Override
    public @NotNull Collection<Placeholder.Equal> getPlaceholders() {
        return placeholders.values();
    }
}
