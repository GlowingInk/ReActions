package me.fromgate.reactions.logic;

import me.fromgate.reactions.data.DataValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Context created per activator
 */
public class RaContext {

    public static final RaContext EMPTY_CONTEXT = new RaContext(":unknown", null, null, null);

    private final String activatorName;
    private final Player player;
    // TODO Use Map<String, Function<String>> instead?
    private final Map<String, String> variables;
    private final Map<String, DataValue> changeables;
    // TODO
    private final boolean async;

    public RaContext(@NotNull String activator, @Nullable Map<String, String> variables, @Nullable Map<String, DataValue> changeables, @Nullable Player player) {
        this(activator, variables, changeables, player, false);
    }

    public RaContext(@NotNull String activator, @Nullable Map<String, String> variables, @Nullable Map<String, DataValue> changeables, @Nullable Player player, boolean async) {
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
        this.activatorName = activator;
        if (changeables == null || changeables.isEmpty()) {
            this.changeables = Map.of();
        } else {
            this.changeables = changeables;
            changeables.keySet().forEach(key -> this.variables.put(key, changeables.get(key).asString()));
        }
        this.player = player;
        this.async = async;
    }

    public @Nullable String getVariable(@NotNull String key) {
        return variables.get(key.toLowerCase(Locale.ROOT));
    }

    @Contract("_, !null -> !null")
    public @Nullable String getVariable(@NotNull String key, @Nullable String def) {
        return variables.getOrDefault(key.toLowerCase(Locale.ROOT), def);
    }

    public @Nullable String setVariable(@NotNull String key, @Nullable String str) {
        return variables.put(key.toLowerCase(Locale.ROOT), str);
    }

    public @Nullable String setVariable(@NotNull String key, @Nullable Object obj) {
        return variables.put(key.toLowerCase(Locale.ROOT), String.valueOf(obj));
    }

    public boolean setChangeable(@NotNull String key, @NotNull String value) {
        key = key.toLowerCase(Locale.ROOT);
        DataValue dataValue = changeables.get(key);
        if (dataValue == null || !dataValue.set(value)) return false;
        variables.put(key, dataValue.asString());
        return true;
    }

    public boolean setChangeable(@NotNull String key, boolean value) {
        key = key.toLowerCase(Locale.ROOT);
        DataValue dataValue = changeables.get(key);
        if (dataValue == null || !dataValue.set(value)) return false;
        variables.put(key, dataValue.asString());
        return true;
    }

    public String getActivatorName() {
        return this.activatorName;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Map<String, String> getVariables() {
        return this.variables;
    }

    public boolean isAsync() {
        return this.async;
    }
}
