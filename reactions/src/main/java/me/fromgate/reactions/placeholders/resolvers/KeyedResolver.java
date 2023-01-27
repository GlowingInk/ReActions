package me.fromgate.reactions.placeholders.resolvers;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public final class KeyedResolver implements Resolver<Placeholder.Keyed> {
    private final Map<String, Placeholder.Keyed> placeholders = new HashMap<>();

    @Override
    public boolean put(@NotNull Placeholder.Keyed ph) {
        String key = ph.getName().toLowerCase(Locale.ROOT);
        if (placeholders.containsKey(key)) return false;
        placeholders.put(key, ph);
        for (String alias : Aliased.getAliasesOf(ph)) {
            placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), ph);
        }
        return true;
    }

    @Override
    public @Nullable String parse(@NotNull Environment context, @NotNull String phText) {
        int index = phText.indexOf(':');
        String key;
        String params;
        if (index == -1) {
            key = phText;
            params = "";
        } else {
            key = phText.substring(0, index);
            params = phText.substring(index + 1);
        }
        Placeholder.Keyed ph = placeholders.get((key = key.toLowerCase(Locale.ROOT)));
        if (ph == null) return null;
        return ph.processPlaceholder(context, key, params);
    }

    @Override
    public @NotNull Collection<Placeholder.Keyed> getPlaceholders() {
        return new HashSet<>(placeholders.values());
    }
}
