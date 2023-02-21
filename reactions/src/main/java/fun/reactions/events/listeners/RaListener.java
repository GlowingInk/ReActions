package fun.reactions.events.listeners;

import fun.reactions.events.PlayerMoveByBlockEvent;
import fun.reactions.events.PlayerPickupItemEvent;
import fun.reactions.events.PlayerStayEvent;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basics.ContextManager;
import fun.reactions.module.basics.ItemContextManager;
import fun.reactions.module.basics.context.DropContext;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class RaListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveByBlockEvent event) {
        ContextManager.triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        ContextManager.triggerCuboid(event.getPlayer());
    }

    @EventHandler
    public void onStay(PlayerStayEvent event) {
        ContextManager.triggerCuboid(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        Optional<Variables> optVars = ContextManager.triggerPickupItem(event.getPlayer(), event.getItem(), event.getItem().getPickupDelay());
        if (optVars.isEmpty()) return;
        Variables vars = optVars.get();
        vars.getChanged(ActivationContext.CANCEL_EVENT, Boolean::valueOf).ifPresent(event::setCancelled);
        vars.getChanged(DropContext.PICKUP_DELAY, NumberUtils::asInteger).ifPresent((d) -> event.getItem().setPickupDelay(d));
        vars.getChanged(DropContext.ITEM, VirtualItem::asItemStack).ifPresent((i) -> event.getItem().setItemStack(i));
        if (event.isCancelled()) return;
        ItemContextManager.triggerItemHold(event.getPlayer());
        ItemContextManager.triggerItemWear(event.getPlayer());
    }
}
