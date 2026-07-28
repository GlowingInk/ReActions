package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static fun.reactions.util.collections.CollectionUtils.caseInsensitiveMap;

public abstract class ComputedVariable extends TrackedVariable {
    private @Nullable Map<String, Variable> childrenCache;

    protected abstract @NotNull List<String> childKeys();

    protected abstract @Nullable Variable computeChild(@NotNull String key);

    protected abstract @NotNull ComputedVariable copy();

    private @NotNull Map<String, Variable> childrenCache() {
        if (childrenCache == null) {
            childrenCache = caseInsensitiveMap();
        }
        return childrenCache;
    }

    @Override
    public @Nullable Variable child(@NotNull String key) {
        return childrenCache().computeIfAbsent(key, k -> computeChild(k.toLowerCase(Locale.ROOT)));
    }

    @Override
    public @NotNull Map<String, Variable> children() {
        for (String key : childKeys()) {
            child(key);
        }
        return childrenCache();
    }

    @Override
    public boolean putChild(@NotNull String key, @NotNull Variable value) {
        childrenCache().put(key, value);
        markChanged();
        return true;
    }

    @Override
    public @NotNull Variable set(@NotNull String value) {
        return Variable.of(value, true);
    }

    @Override
    public @NotNull Variable fork() {
        ComputedVariable forked = copy();
        if (childrenCache != null) {
            var forkedCache = forked.childrenCache();
            for (var entry : childrenCache.entrySet()) {
                forkedCache.put(entry.getKey(), entry.getValue().fork());
            }
        }
        return forked;
    }
}
