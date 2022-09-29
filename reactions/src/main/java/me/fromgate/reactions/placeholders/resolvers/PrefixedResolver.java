package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public final class PrefixedResolver implements Resolver<Placeholder.Prefixed> {
    private final Map<String, Placeholder.Prefixed> placeholders = new HashMap<>();

    @Override
    public boolean put(@NotNull Placeholder.Prefixed ph) {
        String prefix = ph.getName().toLowerCase(Locale.ROOT);
        if (placeholders.containsKey(prefix)) return false;
        placeholders.put(prefix, ph);
        for (String alias : Utils.getAliases(ph))
            placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), ph);
        return true;
    }

    @Override
    public @Nullable String parse(@NotNull RaContext context, @NotNull String text) {
        String[] split = text.split(":", 2);
        if (split.length == 1 || split[1].isEmpty()) return null;
        String prefix = split[0].toLowerCase(Locale.ROOT);
        Placeholder ph = placeholders.get(prefix);
        if (ph == null) return null;
        return ph.processPlaceholder(context, prefix, split[1]);
    }

    @Override
    public @NotNull Collection<Placeholder.Prefixed> getPlaceholders() {
        return new HashSet<>(placeholders.values());
    }
}
