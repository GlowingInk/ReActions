package me.fromgate.reactions.module.basics;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.module.basics.activators.*;
import me.fromgate.reactions.module.basics.storages.*;
import me.fromgate.reactions.util.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * To manage some item-related activators
 */
// TODO: Move to custom ActivatorTypes
public final class ItemStoragesManager {
    private ItemStoragesManager() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    private static void setFutureItemWearCheck(final UUID playerId, final String itemStr, boolean repeat) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        if (!player.isOnline()) return;
        String rg = "iw-" + itemStr;
        if (!StoragesManager.isTimeToRaiseEvent(player, rg, Cfg.itemWearRecheck, repeat)) return;
        ItemWearStorage iwe = new ItemWearStorage(player);
        if (!iwe.isItemWeared(itemStr)) return;
        ReActions.getActivators().activate(iwe);
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureItemWearCheck(playerId, itemStr, true), 20L * Cfg.itemWearRecheck);
    }

    public static void triggerItemWear(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator iw : ReActions.getActivators().getType(ItemWearActivator.class).getActivators())
                setFutureItemWearCheck(playerId, ((ItemWearActivator) iw).getItemStr(), false);
        }, 1);
    }

    public static void triggerItemHold(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator ih : ReActions.getActivators().getType(ItemHoldActivator.class).getActivators())
                setFutureItemHoldCheck(playerId, ((ItemHoldActivator) ih).getItemStr(), false);
        }, 1);
    }

    private static void setFutureItemHoldCheck(final UUID playerId, final String itemStr, boolean repeat) {
        Player player = Bukkit.getPlayer(playerId);

        if (player == null || !player.isOnline() || player.isDead()) return;

        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        boolean mainHandItemExist = isItemHoldProcessable(mainHandItem, itemStr);
        boolean offHandItemExist = isItemHoldProcessable(offHandItem, itemStr);

        if (!mainHandItemExist && !offHandItemExist) return;
        String rg = "ih-" + itemStr;
        if (!StoragesManager.isTimeToRaiseEvent(player, rg, Cfg.itemHoldRecheck, repeat)) return;

        if (mainHandItemExist) processItemHoldActivator(player, mainHandItem, true);
        if (offHandItemExist) processItemHoldActivator(player, offHandItem, false);

        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureItemHoldCheck(playerId, itemStr, true), 20L * Cfg.itemHoldRecheck);
    }

    private static void processItemHoldActivator(Player player, ItemStack item, boolean mainHand) {
        ItemHoldStorage ihe = new ItemHoldStorage(player, item, mainHand);
        ReActions.getActivators().activate(ihe);
    }

    private static boolean isItemHoldProcessable(ItemStack item, String itemStr) {
        return ItemUtils.isExist(item) && ItemUtils.compareItemStr(item, itemStr);
    }
}
