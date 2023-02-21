package me.fromgate.reactions.events.listeners;

import me.fromgate.reactions.events.PlayerMoveByBlockEvent;
import me.fromgate.reactions.events.PlayerPickupItemEvent;
import me.fromgate.reactions.events.PlayerStayEvent;
import me.fromgate.reactions.logic.activators.ActivationContext;
import me.fromgate.reactions.logic.environment.Variables;
import me.fromgate.reactions.module.basics.ItemDetailsManager;
import me.fromgate.reactions.module.basics.details.DropContext;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

import static me.fromgate.reactions.module.basics.DetailsManager.*;

public class RaListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveByBlockEvent event) {
        triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        triggerCuboid(event.getPlayer());
    }

    @EventHandler
    public void onStay(PlayerStayEvent event) {
        triggerCuboid(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        Optional<Variables> optVars = triggerPickupItem(event.getPlayer(), event.getItem(), event.getItem().getPickupDelay());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(DropContext.PICKUP_DELAY, NumberUtils::asInteger).ifPresent((d) -> event.getItem().setPickupDelay(d));
        vars.getChanged(DropContext.ITEM, VirtualItem::asItemStack).ifPresent((i) -> event.getItem().setItemStack(i));
        if (event.isCancelled()) return;
        ItemDetailsManager.triggerItemHold(event.getPlayer());
        ItemDetailsManager.triggerItemWear(event.getPlayer());
    }
}
