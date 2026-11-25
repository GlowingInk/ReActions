package fun.reactions.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MovementRegistry implements MovementHandler {
    private final List<MovementHandler> handlers = new ArrayList<>();

    public void registerHandler(@NotNull MovementHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void onMove(@NotNull Player player, @Nullable Location from, @NotNull Location to) {
        for (MovementHandler handler : handlers) {
            handler.onMove(player, from, to);
        }
    }
}
