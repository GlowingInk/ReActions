package me.fromgate.reactions.time.wait;

import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.logic.context.Variables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static me.fromgate.reactions.logic.ActivatorLogic.executeActions;

public record WaitTask(
        @NotNull Variables variables,
        @Nullable UUID playerId,
        @NotNull List<StoredAction> actions,
        long executionTime
) implements Comparable<WaitTask> {
    public boolean isTime() {
        return System.currentTimeMillis() >= executionTime;
    }

    public void execute() {
        Player player = playerId == null ? null : Bukkit.getPlayer(playerId);
        executeActions(new Environment("", variables, player), actions, player != null);
    }

    @Override
    public int compareTo(@NotNull WaitTask o) {
        return executionTime > o.executionTime ? 1 : -1;
    }
}
