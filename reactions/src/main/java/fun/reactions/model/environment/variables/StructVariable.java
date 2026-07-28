package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static fun.reactions.util.collections.CollectionUtils.caseInsensitiveMap;

public final class StructVariable extends TrackedVariable {
    @ApiStatus.Internal
    public static final String SELF_KEY = "=";

    private String value;
    private final Map<String, Variable> children;

    public StructVariable(@NotNull String value, @NotNull Map<String, Variable> children) {
        this.value = value;
        this.children = caseInsensitiveMap(children);
    }

    @Override
    public @NotNull String get() {
        return value;
    }

    @Override
    public @NotNull Map<String, Variable> children() {
        return children;
    }

    @Override
    public boolean putChild(@NotNull String key, @NotNull Variable value) {
        children.put(key, value);
        markChanged();
        return true;
    }

    @Override
    public @NotNull Variable set(@NotNull String value) {
        this.value = value;
        markChanged();
        return this;
    }

    @Override
    public @NotNull Variable fork() {
        Map<String, Variable> forkedChildren = new HashMap<>(children.size());
        for (var entry : children.entrySet()) {
            forkedChildren.put(entry.getKey(), entry.getValue().fork());
        }
        return new StructVariable(value, forkedChildren);
    }

    @ApiStatus.Internal
    public static @NotNull StructVariable read(@NotNull ConfigurationSection section) {
        String self = section.getString(SELF_KEY, "");
        Map<String, Variable> children = new HashMap<>();
        for (String key : section.getKeys(false)) {
            if (key.equals(SELF_KEY)) continue;
            children.put(
                    key,
                    section.isConfigurationSection(key)
                            ? read(Objects.requireNonNull(section.getConfigurationSection(key)))
                            : Variable.simple(section.getString(key, ""))
            );
        }
        return new StructVariable(self, children);
    }

    public static @NotNull StructVariable ofFlat(@NotNull String self, @NotNull Parameters params) {
        Map<String, Variable> children = new HashMap<>(params.keys().size());
        for (String key : params.keys()) {
            children.put(key, Variable.simple(params.getString(key)));
        }
        return new StructVariable(self, children);
    }

    public static @NotNull Variable fromParameters(@NotNull Parameters params) {
        String self = params.getString("value", "");
        if (!params.contains("children")) {
            return Variable.simple(self);
        }
        Parameters childParams = params.getParameters("children");
        Map<String, Variable> children = new HashMap<>(childParams.keys().size());
        for (String key : childParams.keys()) {
            children.put(key, fromRaw(childParams.getString(key)));
        }
        return new StructVariable(self, children);
    }

    @ApiStatus.Internal
    public static @NotNull Variable fromRaw(@NotNull String raw) {
        Parameters params = Parameters.fromString(raw, "value");
        String self = params.getString("value", "");
        Set<String> keys = new HashSet<>(params.keys());
        keys.removeIf(key -> key.equalsIgnoreCase("value"));
        if (keys.isEmpty()) {
            return Variable.simple(self);
        }
        Map<String, Variable> children = new HashMap<>(keys.size());
        for (String key : keys) {
            children.put(key, fromRaw(params.getString(key)));
        }
        return new StructVariable(self, children);
    }
}
