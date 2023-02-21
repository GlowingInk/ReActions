package fun.reactions.module.basics;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.activators.ItemHoldActivator;
import fun.reactions.module.basics.activators.ItemWearActivator;
import fun.reactions.module.basics.context.ItemHoldContext;
import fun.reactions.module.basics.context.ItemWearContext;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * To manage some item-related activators
 */
// TODO: Move to custom ActivatorTypes
public final class ItemContextManager {
    private ItemContextManager() {}

    private static void setFutureItemWearCheck(final UUID playerId, final String itemStr, boolean repeat) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        if (!player.isOnline()) return;
        String rg = "iw-" + itemStr;
        if (!ContextManager.isTimeToRaiseEvent(player, rg, Cfg.itemWearRecheck, repeat)) return;
        ItemWearContext iwe = new ItemWearContext(player);
        if (!iwe.isItemWeared(itemStr)) return;
        ReActions.getActivators().activate(iwe);
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureItemWearCheck(playerId, itemStr, true), 20L * Cfg.itemWearRecheck);
    }

    public static void triggerItemWear(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator iw : ReActions.getActivatorTypes().get(ItemWearActivator.class).getActivators())
                setFutureItemWearCheck(playerId, ((ItemWearActivator) iw).getItemStr(), false);
        }, 1);
    }

    public static void triggerItemHold(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator ih : ReActions.getActivatorTypes().get(ItemHoldActivator.class).getActivators())
                setFutureItemHoldCheck(playerId, ((ItemHoldActivator) ih).getItem(), false);
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
        if (!ContextManager.isTimeToRaiseEvent(player, rg, Cfg.itemHoldRecheck, repeat)) return;

        if (mainHandItemExist) processItemHoldActivator(player, mainHandItem, EquipmentSlot.HAND);
        if (offHandItemExist) processItemHoldActivator(player, offHandItem, EquipmentSlot.OFF_HAND);

        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureItemHoldCheck(playerId, itemStr, true), 20L * Cfg.itemHoldRecheck);
    }

    private static void processItemHoldActivator(Player player, ItemStack item, EquipmentSlot hand) {
        ItemHoldContext ihe = new ItemHoldContext(player, item, hand);
        ReActions.getActivators().activate(ihe);
    }

    private static boolean isItemHoldProcessable(ItemStack item, String itemStr) {
        return VirtualItem.isSimilar(itemStr, item);
    }
}
