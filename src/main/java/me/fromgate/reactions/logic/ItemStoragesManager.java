package me.fromgate.reactions.logic;

import lombok.experimental.UtilityClass;
import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activators.ItemHoldActivator;
import me.fromgate.reactions.logic.activators.ItemWearActivator;
import me.fromgate.reactions.logic.storages.ItemHoldStorage;
import me.fromgate.reactions.logic.storages.ItemWearStorage;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * To manage some item-related activators
 */
@UtilityClass
public class ItemStoragesManager {
    // TODO: Recode.

    private void setFutureItemWearCheck(final UUID playerId, final String itemStr, boolean repeat) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;
        if (!player.isOnline()) return;
        String rg = "iw-" + itemStr;
        if (!StoragesManager.isTimeToRaiseEvent(player, rg, Cfg.itemWearRecheck, repeat)) return;
        ItemWearStorage iwe = new ItemWearStorage(player);
        if (!iwe.isItemWeared(itemStr)) return;
        ReActions.getActivators().activate(iwe);
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureItemWearCheck(playerId, itemStr, true), 20 * Cfg.itemWearRecheck);
    }

    public void triggerItemWear(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator iw : ReActions.getActivators().getActivators(ActivatorType.ITEM_WEAR))
                setFutureItemWearCheck(playerId, ((ItemWearActivator) iw).getItemStr(), false);
        }, 1);
    }

    public void triggerItemHold(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator ih : ReActions.getActivators().getActivators(ActivatorType.ITEM_HOLD))
                setFutureItemHoldCheck(playerId, ((ItemHoldActivator) ih).getItemStr(), false);
        }, 1);
    }

    private void setFutureItemHoldCheck(final UUID playerId, final String itemStr, boolean repeat) {
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

        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureItemHoldCheck(playerId, itemStr, true), 20 * Cfg.itemHoldRecheck);
    }

    private void processItemHoldActivator(Player player, ItemStack item, boolean mainHand) {
        ItemHoldStorage ihe = new ItemHoldStorage(player, item, mainHand);
        ReActions.getActivators().activate(ihe);
    }

    private boolean isItemHoldProcessable(ItemStack item, String itemStr) {
        return ItemUtils.isExist(item) && ItemUtils.compareItemStr(item, itemStr);
    }
}
