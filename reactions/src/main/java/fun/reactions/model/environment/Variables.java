package fun.reactions.model.environment;

import fun.reactions.util.bool.OptionalBoolean;
import fun.reactions.util.bool.TriBoolean;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.*;

public class Variables {
    private final Map<String, Variable> variables;

    @ApiStatus.Internal
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
        Set<String> keys = params.keys();
        Map<String, Variable> vars = new HashMap<>(keys.size());
        for (String key : keys) {
            vars.put(key.toLowerCase(Locale.ROOT), Variable.simple(params.getString(key)));
        }
        return new Variables(vars, false);
    }

    public @NotNull Map<String, Variable> forkMap() {
        Map<String, Variable> forkedVars = new HashMap<>(variables.size());
        if (!variables.isEmpty()) {
            for (var entry : variables.entrySet()) {
                forkedVars.put(entry.getKey(), entry.getValue().fork());
            }
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

    public <T> @NotNull Optional<T> changed(@NotNull String key, @NotNull Function<String, T> funct) {
        return changedString(key).map(funct);
    }

    public @NotNull Optional<String> changedString(@NotNull String key) {
        return variables.getOrDefault(key.toLowerCase(Locale.ROOT), Variable.EMPTY).changed();
    }

    public @NotNull OptionalBoolean changedBoolean(@NotNull String key) {
        return changedString(key).map(s -> TriBoolean.byString(s).asOptional()).orElse(OptionalBoolean.EMPTY);
    }

    public @NotNull OptionalBoolean changedBoolean(@NotNull String key, @NotNull Predicate<@NotNull String> funct) {
        return changedString(key).map(s -> OptionalBoolean.of(funct.test(s))).orElse(OptionalBoolean.EMPTY);
    }

    public @NotNull OptionalInt changedInt(@NotNull String key) {
        return changedString(key).map(NumberUtils::parseInteger).orElse(OptionalInt.empty());
    }

    public @NotNull OptionalInt changedInt(@NotNull String key, @NotNull ToIntFunction<@NotNull String> funct) {
        return changedString(key).map(s -> OptionalInt.of(funct.applyAsInt(s))).orElse(OptionalInt.empty());
    }

    public @NotNull OptionalLong changedLong(@NotNull String key) {
        return changedString(key).map(NumberUtils::parseLong).orElse(OptionalLong.empty());
    }

    public @NotNull OptionalLong changedLong(@NotNull String key, @NotNull ToLongFunction<@NotNull String> funct) {
        return changedString(key).map(s -> OptionalLong.of(funct.applyAsLong(s))).orElse(OptionalLong.empty());
    }

    public @NotNull OptionalDouble changedDouble(@NotNull String key) {
        return changedString(key).map(NumberUtils::parseDouble).orElse(OptionalDouble.empty());
    }

    public @NotNull OptionalDouble changedDouble(@NotNull String key, @NotNull ToDoubleFunction<@NotNull String> funct) {
        return changedString(key).map(s -> OptionalDouble.of(funct.applyAsDouble(s))).orElse(OptionalDouble.empty());
    }

    public void set(@NotNull String key, @Nullable String value) {
        key = key.toLowerCase(Locale.ROOT);
        if (value == null) {
            variables.remove(key);
        } else {
            variables.put(key, variables.getOrDefault(key.toLowerCase(Locale.ROOT), Variable.EMPTY).set(value));
        }
    }

    public @UnmodifiableView @NotNull Set<String> keys() {
        return Collections.unmodifiableSet(variables.keySet());
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }
}
