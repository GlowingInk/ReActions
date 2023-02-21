package fun.reactions.time.wait;

import fun.reactions.logic.Logic;
import fun.reactions.logic.activity.actions.StoredAction;
import fun.reactions.logic.environment.Environment;
import fun.reactions.logic.environment.Variables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

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
        Logic.executeActions(new Environment("", variables, player), actions, player != null);
    }

    @Override
    public int compareTo(@NotNull WaitTask o) {
        return executionTime > o.executionTime ? 1 : -1;
    }
}
