package fun.reactions.time.wait;

import fun.reactions.ReActions;
import fun.reactions.model.Logic;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record WaitTask(
        @NotNull Variables variables,
        @Nullable UUID playerId,
        @NotNull List<Action.Stored> actions,
        long executionTime
) implements Comparable<WaitTask> {
    public boolean isTime() {
        return System.currentTimeMillis() >= executionTime;
    }

    public void execute(@NotNull ReActions.Platform platform) {
        Player player = playerId == null ? null : Bukkit.getPlayer(playerId);
        Logic.executeActions(new Environment(platform, "", variables, player), actions, player != null);
    }

    @Override
    public int compareTo(@NotNull WaitTask o) {
        return executionTime > o.executionTime ? 1 : -1;
    }
}
