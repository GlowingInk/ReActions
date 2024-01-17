package fun.reactions.placeholders.resolvers;

import fun.reactions.model.environment.Environment;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class KeyedResolver implements PlaceholderResolver<Placeholder.Keyed> {
    private final Map<String, Placeholder.Keyed> placeholders = new HashMap<>();

    @Override
    public boolean add(@NotNull Placeholder.Keyed ph) {
        String key = ph.getName().toLowerCase(Locale.ROOT);
        if (placeholders.containsKey(key)) return false;
        placeholders.put(key, ph);
        for (String alias : Aliased.getAliasesOf(ph)) {
            placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), ph);
        }
        return true;
    }

    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
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
        return ph.resolve(env, key, params);
    }

    @Override
    public @NotNull Collection<Placeholder.Keyed> getPlaceholders() {
        return new HashSet<>(placeholders.values());
    }
}
