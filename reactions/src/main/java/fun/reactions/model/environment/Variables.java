package fun.reactions.model.environment;

import fun.reactions.model.environment.variables.StructVariable;
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

import static fun.reactions.util.collections.CollectionUtils.caseInsensitiveMap;

public class Variables {
    private final Map<String, Variable> variables;

    @ApiStatus.Internal
    public Variables(@NotNull Map<String, Variable> variables) {
        this(variables, true);
    }

    private Variables(@NotNull Map<String, Variable> variables, boolean copy) {
        this.variables = copy ? caseInsensitiveMap(variables) : variables;
    }

    public Variables() {
        this.variables = caseInsensitiveMap();
    }

    @ApiStatus.Internal
    public static @NotNull Variables readConfiguration(@NotNull ConfigurationSection cfg) {
        Set<String> keys = cfg.getKeys(false);
        Map<String, Variable> vars = caseInsensitiveMap(keys.size());
        for (String key : keys) {
            Variable var = cfg.isConfigurationSection(key)
                    ? StructVariable.read(Objects.requireNonNull(cfg.getConfigurationSection(key)))
                    : Variable.value(cfg.getString(key, ""));
            vars.put(key, var);
        }
        return new Variables(vars, false);
    }

    @ApiStatus.Internal
    public void writeConfiguration(@NotNull ConfigurationSection cfg) {
        for (var entry : variables.entrySet()) {
            writeVariable(cfg, entry.getKey(), entry.getValue());
        }
    }

    private static void writeVariable(@NotNull ConfigurationSection cfg, @NotNull String key, @NotNull Variable var) {
        Map<String, Variable> children = var.children();
        if (children.isEmpty()) {
            cfg.set(key, var.get());
            return;
        }
        ConfigurationSection section = cfg.createSection(key);
        section.set(StructVariable.SELF_KEY, var.get());
        for (var childEntry : children.entrySet()) {
            writeVariable(section, childEntry.getKey(), childEntry.getValue());
        }
    }

    @ApiStatus.Internal
    public static @NotNull Variables readParameters(@NotNull Parameters params) {
        Set<String> keys = params.keys();
        Map<String, Variable> vars = caseInsensitiveMap(keys.size());
        for (String key : keys) {
            vars.put(key, Variable.value(params.getString(key)));
        }
        return new Variables(vars, false);
    }

    public @NotNull Map<String, Variable> forkMap() {
        Map<String, Variable> forkedVars = caseInsensitiveMap(variables.size());
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
        return variables.getOrDefault(key, Variable.EMPTY);
    }

    private @Nullable Variable getUnsafe(@NotNull String key) {
        return variables.get(key);
    }

    public @Nullable Variable getVariable(@NotNull String key) {
        return getUnsafe(key);
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
        return variables.getOrDefault(key, Variable.EMPTY).changed();
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

    public void setVariable(@NotNull String key, @NotNull Variable variable) {
        variables.put(key, variable);
    }

    public void set(@NotNull String key, @Nullable String value) {
        if (value == null) {
            variables.remove(key);
        } else {
            variables.put(key, variables.getOrDefault(key, Variable.EMPTY).set(value));
        }
    }

    public @UnmodifiableView @NotNull Set<String> keys() {
        return Collections.unmodifiableSet(variables.keySet());
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }
}
