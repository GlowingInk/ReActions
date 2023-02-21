package fun.reactions.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class PlayerAttacksEntityEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity entity;
    private final EntityDamageEvent.DamageCause cause;
    private boolean cancel = false;
    private double damage;
    private final double finalDamage;

    public PlayerAttacksEntityEvent(Player player, LivingEntity entity, double damage, double finalDamage, EntityDamageEvent.DamageCause cause) {
        super(player);
        this.entity = entity;
        this.damage = damage;
        this.finalDamage = finalDamage;
        this.cause = cause;
    }

    public LivingEntity getEntity() {return this.entity;}

    public EntityDamageEvent.DamageCause getCause() {return this.cause;}

    public double getDamage() {return this.damage;}

    public void setDamage(double damage) {this.damage = damage; }

    public double getFinalDamage() {
        return finalDamage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}