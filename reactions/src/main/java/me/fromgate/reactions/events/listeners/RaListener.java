package me.fromgate.reactions.events.listeners;

import me.fromgate.reactions.events.PlayerMoveByBlockEvent;
import me.fromgate.reactions.events.PlayerPickupItemEvent;
import me.fromgate.reactions.events.PlayerStayEvent;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.ItemStoragesManager;
import me.fromgate.reactions.module.basics.storages.PickupItemStorage;
import me.fromgate.reactions.util.data.DataValue;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

import static me.fromgate.reactions.module.basics.StoragesManager.*;

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
        Map<String, DataValue> changeables = triggerPickupItem(event.getPlayer(), event.getItem(), event.getItem().getPickupDelay());
        if (changeables == null) return;
        event.getItem().setPickupDelay((int) changeables.get(PickupItemStorage.PICKUP_DELAY).asDouble());
        event.getItem().setItemStack(changeables.get(PickupItemStorage.ITEM).asItemStack());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
        if (event.isCancelled()) return;
        ItemStoragesManager.triggerItemHold(event.getPlayer());
        ItemStoragesManager.triggerItemWear(event.getPlayer());
    }
}
