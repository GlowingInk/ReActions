package fun.reactions.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface MovementHandler {
    void onMove(@NotNull Player player, @Nullable Location from, @NotNull Location to);
}
