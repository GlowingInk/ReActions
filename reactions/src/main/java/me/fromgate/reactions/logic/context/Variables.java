package me.fromgate.reactions.logic.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class Variables {
    public static final Variables UNMODIFIABLE = new Variables(Map.of()) {
        @Override
        public boolean isInitialized() {
            return false;
        }
    };

    private final Map<String, Variable> variables;

    public Variables(Map<String, Variable> variables) {
        this.variables = new HashMap<>(variables);
    }

    public Variables() {
        this.variables = new HashMap<>();
    }

    public @NotNull Map<String, Variable> forkMap() {
        Map<String, Variable> forkedVars = new HashMap<>(variables.size());
        for (var entry : variables.entrySet()) {
            forkedVars.put(entry.getKey(), entry.getValue().fork());
        }
        return forkedVars;
    }

    public @NotNull Variables fork() {
        return new Variables(forkMap());
    }

    public @NotNull Variable get(@NotNull String key) {
        return variables.getOrDefault(key.toLowerCase(Locale.ROOT), Variable.EMPTY);
    }

    public @Nullable Variable getUnsafe(@NotNull String key) {
        return variables.get(key.toLowerCase(Locale.ROOT));
    }

    public @NotNull String getString(@NotNull String key) {
        return get(key).get();
    }

    public @Nullable String getUnsafeString(@NotNull String key) {
        Variable vari = getUnsafe(key);
        return vari == null ? null : vari.get();
    }

    public @NotNull Optional<String> getChanged(@NotNull String key) {
        return variables.getOrDefault(key.toLowerCase(Locale.ROOT), Variable.EMPTY).getChanged();
    }

    public <T> @NotNull Optional<T> getChanged(@NotNull String key, @NotNull Function<String, T> funct) {
        return getChanged(key).map(funct);
    }

    public @NotNull Optional<String> getChecked(@NotNull String key, @NotNull Predicate<String> filter) {
        return getChanged(key).filter(filter);
    }

    public <T> @NotNull Optional<T> getChecked(@NotNull String key, @NotNull Function<String, T> funct, @NotNull Predicate<T> filter) {
        return getChanged(key).map(funct).filter(filter);
    }

    public <T> @NotNull Optional<T> getPrechecked(@NotNull String key, @NotNull Predicate<String> filter, @NotNull Function<String, T> funct) {
        return getChanged(key).filter(filter).map(funct);
    }

    public void set(@NotNull String key, @Nullable String str) {
        key = key.toLowerCase(Locale.ROOT);
        if (str == null) {
            variables.remove(key);
        } else {
            variables.put(key, variables.getOrDefault(key.toLowerCase(Locale.ROOT), Variable.EMPTY).set(str));
        }
    }

    public boolean isInitialized() {
        return true;
    }
}
