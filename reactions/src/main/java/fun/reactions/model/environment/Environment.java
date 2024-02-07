package fun.reactions.model.environment;

import fun.reactions.ReActions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context created per activator
 */
public final class Environment {
    private final ReActions.Platform platform;
    private final String activatorName;
    private final Player player;

    private final Variables variables;
    private final boolean async; // TODO

    public Environment(@NotNull ReActions.Platform platform, @NotNull String activator, @NotNull Variables variables, @Nullable Player player) {
        this(platform, activator, variables, player, false);
    }

    public Environment(@NotNull ReActions.Platform platform, @NotNull String activator, @NotNull Variables variables, @Nullable Player player, boolean async) {
        this.platform = platform;
        this.variables = variables;
        this.activatorName = activator;
        this.player = player;
        this.async = async;
    }

    public @NotNull ReActions.Platform getPlatform() {
        return platform;
    }

    public @NotNull String getActivatorName() {
        return this.activatorName;
    }

    public @Nullable Player getPlayer() {
        return this.player;
    }

    public boolean hasPlayer() {
        return this.player != null;
    }

    public @NotNull Variables getVariables() {
        return variables;
    }

    public boolean isAsync() {
        return this.async;
    }

    public void warn(@NotNull String msg) {
        platform.logger().warn(activatorName + " | " + msg);
    }

    public void warn(@NotNull Component msg) {
        platform.logger().warn(Component.text(activatorName).append(Component.text(" | ")).append(msg));
    }
}
