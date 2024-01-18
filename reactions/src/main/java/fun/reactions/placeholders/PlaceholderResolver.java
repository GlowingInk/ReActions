package fun.reactions.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class PlaceholderResolver {
    private final Map<String, Placeholder> placeholders = new HashMap<>();
    private final List<Placeholder.Dynamic> dynamicPlaceholders = new ArrayList<>();

    public boolean add(@NotNull Placeholder ph) {
        String key = ph.getName().toLowerCase(Locale.ROOT);
        boolean registered = false;
        if (!placeholders.containsKey(key)) {
            placeholders.put(key, ph);
            registered = true;
            for (String alias : Aliased.getAliasesOf(ph)) {
                placeholders.putIfAbsent(alias.toLowerCase(Locale.ROOT), ph);
            }
        }
        if (ph instanceof Placeholder.Dynamic phDynamic) {
            registered = true;
            dynamicPlaceholders.add(phDynamic);
        }
        return registered;
    }

    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
        String resolved = resolveKeyed(env, phText);
        return resolved == null
                ? resolveDynamic(env, phText)
                : resolved;
    }

    private @Nullable String resolveKeyed(@NotNull Environment env, @NotNull String phText) {
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
        Placeholder ph = placeholders.get((key = key.toLowerCase(Locale.ROOT)));
        if (ph == null) return null;
        return ph.resolve(env, key, params);
    }

    private @Nullable String resolveDynamic(@NotNull Environment env, @NotNull String phText) {
        for (Placeholder.Dynamic ph : dynamicPlaceholders) {
            String result = ph.resolve(env, phText);
            if (result != null) return result;
        }
        return null;
    }
}
