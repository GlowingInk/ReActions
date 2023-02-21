package fun.reactions.module.basics.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class PlayerMoveByBlockEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Location to;
    private final Location from;

    public PlayerMoveByBlockEvent(Player player, Location to, Location from) {
        super(player);
        this.to = to;
        this.from = from;
    }

    public Location getTo() {return this.to;}

    public Location getFrom() {return this.from;}

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
