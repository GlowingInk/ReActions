package fun.reactions.events.listeners;

import fun.reactions.events.PlayerMoveByBlockEvent;
import fun.reactions.events.PlayerPickupItemEvent;
import fun.reactions.events.PlayerStayEvent;
import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.environment.Variables;
import fun.reactions.module.basics.DetailsManager;
import fun.reactions.module.basics.ItemDetailsManager;
import fun.reactions.module.basics.details.DropContext;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class RaListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveByBlockEvent event) {
        DetailsManager.triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        DetailsManager.triggerCuboid(event.getPlayer());
    }

    @EventHandler
    public void onStay(PlayerStayEvent event) {
        DetailsManager.triggerCuboid(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        Optional<Variables> optVars = DetailsManager.triggerPickupItem(event.getPlayer(), event.getItem(), event.getItem().getPickupDelay());
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
