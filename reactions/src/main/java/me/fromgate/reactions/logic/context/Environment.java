package me.fromgate.reactions.logic.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context created per activator
 */
public class Environment {
    private final String activatorName;
    private final Player player;

    private final Variables variables;
    private final boolean async; // TODO

    public Environment(@NotNull String activator, @NotNull Variables variables, @Nullable Player player) {
        this(activator, variables, player, false);
    }

    public Environment(@NotNull String activator, @NotNull Variables variables, @Nullable Player player, boolean async) {
        this.variables = variables;
        this.activatorName = activator;
        this.player = player;
        this.async = async;
    }

    public static @NotNull Environment anonymous() {
        return new Environment("", new Variables(), null);
    }

    public @NotNull String getActivatorName() {
        return this.activatorName;
    }

    public @Nullable Player getPlayer() {
        return this.player;
    }

    public @NotNull Variables getVariables() {
        return variables;
    }

    public boolean isAsync() {
        return this.async;
    }
}
