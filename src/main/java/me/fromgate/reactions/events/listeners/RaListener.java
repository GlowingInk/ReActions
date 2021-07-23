package me.fromgate.reactions.events.listeners;

import me.fromgate.reactions.events.PlayerMoveByBlockEvent;
import me.fromgate.reactions.events.PlayerPickupItemEvent;
import me.fromgate.reactions.events.PlayerStayEvent;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.ItemStoragesManager;
import me.fromgate.reactions.module.basics.StoragesManager;
import me.fromgate.reactions.module.basics.storages.PickupItemStorage;
import me.fromgate.reactions.util.data.DataValue;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class RaListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveByBlockEvent event) {
        StoragesManager.triggerAllRegions(event.getPlayer(), event.getTo(), event.getFrom());
        StoragesManager.triggerCuboid(event.getPlayer());
    }

    @EventHandler
    public void onStay(PlayerStayEvent event) {
        StoragesManager.triggerCuboid(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(PlayerPickupItemEvent event) {
        Map<String, DataValue> changeables = StoragesManager.triggerPickupItem(event.getPlayer(), event.getItem(), event.getItem().getPickupDelay());
        event.getItem().setPickupDelay((int) changeables.get(PickupItemStorage.PICKUP_DELAY).asDouble());
        event.getItem().setItemStack(changeables.get(PickupItemStorage.ITEM).asItemStack());
        event.setCancelled(changeables.get(Storage.CANCEL_EVENT).asBoolean());
        if (event.isCancelled()) return;
        ItemStoragesManager.triggerItemHold(event.getPlayer());
        ItemStoragesManager.triggerItemWear(event.getPlayer());
    }
}
