/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

// TODO: Rewrite
public class FlagItem implements Flag {
    private final Type flagType;

    public FlagItem(Type flagType) {
        this.flagType = flagType;
    }

    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        switch (flagType) {
            case HAND:
                ItemStack item = player.getInventory().getItemInMainHand();
                context.setVariable("item_amount", String.valueOf(item.getAmount())); // TODO: Generalize those weird quirks
                return VirtualItem.fromString(params).isSimilar(item);
            case INVENTORY:
                return hasItemInInventory(context, params);
            case WEAR:
                return isItemWeared(player, params);
            case OFFHAND:
                ItemStack inOffhand = player.getInventory().getItemInOffHand();
                context.setVariable("item_amount", String.valueOf(inOffhand.getAmount()));
                return VirtualItem.fromString(params).isSimilar(inOffhand);
        }
        return false;
    }

    private boolean isItemWeared(Player player, String itemStr) {
        VirtualItem compared = VirtualItem.fromString(itemStr);
        for (ItemStack armour : player.getInventory().getArmorContents())
            if (compared.isSimilar(armour)) return true;
        return false;
    }

    private boolean hasItemInInventory(RaContext context, String itemStr) {
        Player player = context.getPlayer();
        Parameters params = Parameters.fromString(itemStr);

        if (!params.containsEvery("slot", "item")) {
            int countAmount = countItemsInInventory(player.getInventory(), itemStr);
            context.setVariable("item_amount", countAmount == 0 ? "0" : String.valueOf(countAmount));
            int amount = params.getInteger("amount", 1);
            return countAmount >= amount;
        }

        String slotStr = params.getString("slot", "");
        if (slotStr.isEmpty()) return false;
        int slotNum = NumberUtils.asInteger(slotStr, -1);
        if (slotNum >= player.getInventory().getSize()) return false;

        VirtualItem item = VirtualItem.fromParameters(params);

        if (slotNum < 0) {
            return switch (slotStr.toLowerCase(Locale.ROOT)) {
                case "helm", "helmet" -> item.isSimilar(player.getInventory().getHelmet());
                case "chest", "chestplate" -> item.isSimilar(player.getInventory().getChestplate());
                case "legs", "leggings" -> item.isSimilar(player.getInventory().getLeggings());
                case "boot", "boots" -> item.isSimilar(player.getInventory().getBoots());
                default -> false;
            };
        } else return item.isSimilar(player.getInventory().getItem(slotNum));
    }

    @Override
    public @NotNull String getName() {
        return switch (flagType) {
            case HAND -> "ITEM";
            case OFFHAND -> "ITEM_OFFHAND";
            case INVENTORY -> "ITEM_INVENTORY";
            case WEAR -> "ITEM_WEAR";
        };
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private static int countItemsInInventory(Inventory inventory, String itemStr) {
        VirtualItem virtualItem = VirtualItem.fromString(itemStr);
        int count = 0;
        for (ItemStack item : inventory) {
            if (virtualItem.isSimilar(item)) {
                count += item == null ? 1 : item.getAmount();
            }
        }
        return count;
    }

    public enum Type {
        HAND, INVENTORY, WEAR, OFFHAND
    }
}
