package me.fromgate.reactions.module.basics;

import lombok.experimental.UtilityClass;
import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.module.basics.activators.ItemHoldActivator;
import me.fromgate.reactions.module.basics.activators.ItemWearActivator;
import me.fromgate.reactions.module.basics.storages.ItemHoldStorage;
import me.fromgate.reactions.module.basics.storages.ItemWearStorage;
import me.fromgate.reactions.util.item.ItemUtils;
import org.bukkit.Bukkit;
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

    // TODO Move to custom ActivatorType
    public void triggerItemWear(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator iw : ReActions.getActivators().getType(ItemWearActivator.class).getActivators())
                setFutureItemWearCheck(playerId, ((ItemWearActivator) iw).getItemStr(), false);
        }, 1);
    }

    public void triggerItemHold(Player player) {
        final UUID playerId = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (Activator ih : ReActions.getActivators().getType(ItemHoldActivator.class).getActivators())
                setFutureItemHoldCheck(playerId, ((ItemHoldActivator) ih).getItemStr(), false);
        }, 1);
    }

    private void setFutureItemHoldCheck(final UUID playerId, final String itemStr, boolean repeat) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline() || player.isDead()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!ItemUtils.isExist(item)) return;
        String rg = "ih-" + itemStr;
        if (!StoragesManager.isTimeToRaiseEvent(player, rg, Cfg.itemHoldRecheck, repeat)) return;
        if (!ItemUtils.compareItemStr(item, itemStr)) return;
        ItemHoldStorage ihe = new ItemHoldStorage(player, item, true);
        ReActions.getActivators().activate(ihe);

        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> setFutureItemHoldCheck(playerId, itemStr, true), 20 * Cfg.itemHoldRecheck);
    }
}
