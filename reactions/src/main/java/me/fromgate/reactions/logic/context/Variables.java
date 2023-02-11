package me.fromgate.reactions.logic.context;

import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class Variables {
    private final Map<String, Variable> variables;

    public Variables(@NotNull Map<String, Variable> variables) {
        this(variables, true);
    }

    private Variables(@NotNull Map<String, Variable> variables, boolean copy) {
        this.variables = copy ? new HashMap<>(variables) : variables;
    }

    public Variables() {
        this.variables = new HashMap<>(0);
    }

    public static @NotNull Variables readConfiguration(@NotNull ConfigurationSection cfg) {
        Set<String> keys = cfg.getKeys(false);
        Map<String, Variable> vars = new HashMap<>(keys.size());
        for (String key : keys) {
            vars.put(key.toLowerCase(Locale.ROOT), Variable.simple(cfg.getString(key, "")));
        }
        return new Variables(vars, false);
    }

    public static @NotNull Variables readParameters(@NotNull Parameters params) {
        Set<String> keys = params.keysStrict();
        Map<String, Variable> vars = new HashMap<>(keys.size());
        for (String key : keys) {
            vars.put(key.toLowerCase(Locale.ROOT), Variable.simple(params.getString(key)));
        }
        return new Variables(vars, false);
    }

    public @NotNull Map<String, Variable> forkMap() {
        Map<String, Variable> forkedVars = new HashMap<>(variables.size());
        for (var entry : variables.entrySet()) {
            forkedVars.put(entry.getKey(), entry.getValue().fork());
        }
        return forkedVars;
    }

    public @NotNull Variables fork() {
        return new Variables(forkMap(), false);
    }

    private @NotNull Variable get(@NotNull String key) {
        return variables.getOrDefault(key.toLowerCase(Locale.ROOT), Variable.EMPTY);
    }

    private @Nullable Variable getUnsafe(@NotNull String key) {
        return variables.get(key.toLowerCase(Locale.ROOT));
    }

    public @NotNull String getString(@NotNull String key) {
        return get(key).get();
    }

    public @Nullable String getStringUnsafe(@NotNull String key) {
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

    public @UnmodifiableView @NotNull Set<String> keys() {
        return Collections.unmodifiableSet(variables.keySet());
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }
}
