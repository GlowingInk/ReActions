package me.fromgate.reactions.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Deprecated
public class PlayerStayEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Location stay;

    public PlayerStayEvent(Player player, Location stay) {
        super(player);
        this.stay = stay;
    }

    public Location getStay() {return this.stay;}

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
