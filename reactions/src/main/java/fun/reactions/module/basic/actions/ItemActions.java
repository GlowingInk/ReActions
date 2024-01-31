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

package fun.reactions.module.basic.actions;

import fun.reactions.ReActions;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.basic.ItemContextManager;
import fun.reactions.util.Utils;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

// TODO: Remake from scratch
public class ItemActions implements Action, Activity.Personal {
    private final Type actionType;

    public ItemActions(Type actionType) {
        this.actionType = actionType;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        return switch (actionType) {
            case GIVE_ITEM -> giveItemPlayer(env, params.origin());
            case REMOVE_ITEM_HAND -> removeItemInHand(env, params);
            case REMOVE_ITEM_OFFHAND -> removeItemInOffHand(env, params);
            case REMOVE_ITEM_INVENTORY -> removeItemInInventory(env, params);
            case DROP_ITEM -> dropItems(env, params);
            case WEAR_ITEM -> wearItem(env, params);
            case OPEN_INVENTORY -> openInventory(env, params.origin());
            case SET_INVENTORY -> setInventorySlot(env, params);
            case GET_INVENTORY -> getInventorySlot(env, params);
            case UNWEAR_ITEM -> unwearItem(env, params);
        };
    }

    @Override
    public @NotNull String getName() {
        return switch (actionType) {
            case GIVE_ITEM -> "ITEM_GIVE";
            case REMOVE_ITEM_HAND -> "ITEM_REMOVE";
            case REMOVE_ITEM_OFFHAND -> "ITEM_REMOVE_OFFHAND";
            case REMOVE_ITEM_INVENTORY -> "ITEM_REMOVE_INVENTORY";
            case DROP_ITEM -> "ITEM_DROP";
            case WEAR_ITEM -> "ITEM_WEAR";
            case OPEN_INVENTORY -> "OPEN_INVENTORY";
            case SET_INVENTORY -> "ITEM_SLOT";
            case GET_INVENTORY -> "ITEM_SLOT_VIEW";
            case UNWEAR_ITEM -> "ITEM_UNWEAR";
        };
    }

    private boolean setInventorySlot(Environment env, Parameters params) {
        Player player = env.getPlayer();
        String itemStr = params.getString("item");
        if (itemStr.isEmpty()) return false;
        String slotStr = params.getString("slot");
        if (slotStr.isEmpty()) return false;
        OptionalInt slotOpt = NumberUtils.parseInteger(slotStr, Is.NON_NEGATIVE);
        if (slotOpt.isEmpty()) return wearItem(env, params);
        int slotNum = slotOpt.getAsInt();
        if (slotNum >= player.getInventory().getSize()) return false;
        ItemStack oldItem = player.getInventory().getItem(slotNum);
        if (oldItem != null) oldItem = oldItem.clone();
        if (itemStr.equalsIgnoreCase("AIR")) {
            player.getInventory().setItem(slotNum, null);
        } else {
            ItemStack vi = VirtualItem.asItemStack(itemStr);
            if (vi == null) return false;
            player.getInventory().setItem(slotNum, vi);
        }
        if (!ItemUtils.isExist(oldItem)) return true;
        switch (params.getEnum("exist", ItemPolicy.REMOVE)) {
            case DROP -> player.getWorld().dropItemNaturally(player.getLocation(), oldItem);
            case UNDRESS -> ItemUtils.giveItemOrDrop(player, oldItem);
            case KEEP -> player.getInventory().setItem(slotNum, oldItem);
        }
        String actionItems = ItemUtils.toDisplayString(itemStr);
        env.getVariables().set("item_str", actionItems);

        return true;
    }

    private boolean getInventorySlot(Environment env, Parameters params) {
        Player player = env.getPlayer();
        String slotStr = params.getString("slot");
        OptionalInt slotOpt = NumberUtils.parseInteger(slotStr, Is.NON_NEGATIVE);
        if (slotOpt.isEmpty()) return wearItemView(env, params);
        int slotNum = slotOpt.getAsInt();
        if (slotNum >= player.getInventory().getSize()) return false;
        ItemStack item = player.getInventory().getItem(slotNum);
        String actionItems = "";
        if (item != null) actionItems = VirtualItem.asString(item);
        env.getVariables().set("item_str", actionItems);
        env.getVariables().set("item_str_esc", Utils.escapeJava(actionItems));

        return true;
    }

    private boolean wearItemView(Environment env, Parameters params) {
        Player player = env.getPlayer();
        int slot; //4 - auto, 3 - helmet, 2 - chestplate, 1 - leggins, 0 - boots
        slot = getSlotNum(params.getString("slot", "auto"));
        if (slot == -1) return getItemInOffhand(env, params);
        ItemStack[] armour = player.getInventory().getArmorContents();
        ItemStack item = armour[slot];
        String actionItems = "";
        if (item != null) actionItems = VirtualItem.asString(item);
        env.getVariables().set("item_str", actionItems);
        env.getVariables().set("item_str_esc", Utils.escapeJava(actionItems));
        return true;
    }

    private boolean getItemInOffhand(Environment env, Parameters params) {
        Player player = env.getPlayer();
        String itemStr = params.getString("slot");
        if (itemStr.isEmpty()) return false;
        if (!itemStr.equalsIgnoreCase("offhand")) {
            env.getVariables().set("item_str", "");
            env.getVariables().set("item_str_esc", "");
            return true;
        }
        String item = VirtualItem.asString(player.getInventory().getItemInOffHand());
        env.getVariables().set("item_str", item);
        env.getVariables().set("item_str_esc", Utils.escapeJava(item));
        return true;
    }

    private boolean wearItem(Environment env, Parameters params) {
        Player player = env.getPlayer();
        String itemStr = params.getString("item");
        int slot = -1; //4 - auto, 3 - helmete, 2 - chestplate, 1 - leggins, 0 - boots
        ItemPolicy existDrop = ItemPolicy.UNDRESS;
        if (itemStr.isEmpty()) {
            itemStr = params.origin();
        } else {
            slot = getSlotNum(params.getString("slot", "auto"));
            existDrop = params.getEnum("exist", ItemPolicy.UNDRESS);
        }
        ItemStack item = null;
        if (itemStr.equalsIgnoreCase("AIR") || itemStr.equalsIgnoreCase("NULL")) {
            if (slot == -1) return setItemInOffhand(env, params, null);
            //if (slot == -1) slot = 3;
        } else {
            item = VirtualItem.asItemStack(itemStr);
            if (item == null) return false;
            if (slot == -1) return setItemInOffhand(env, params, item);
            // if (slot == -1) slot = getSlotByItem(item);
        }
        return setArmourItem(player, slot, item, existDrop);
    }

    private enum ItemPolicy {
        REMOVE, UNDRESS, DROP, KEEP
    }

    private boolean setItemInOffhand(Environment env, Parameters params, ItemStack item) {
        Player player = env.getPlayer();
        String itemStr = params.getString("slot");
        if (itemStr.isEmpty()) return false;
        if (!itemStr.equalsIgnoreCase("offhand")) return false;
        player.getInventory().setItemInOffHand(item);
        ItemContextManager.triggerItemWear(player);
        return true;
    }

    private boolean setArmourItem(Player player, int slot, ItemStack item, ItemPolicy existDrop) {
        ItemStack[] armour = player.getInventory().getArmorContents().clone();
        ItemStack oldItem = armour[slot] == null ? null : armour[slot].clone();
        if (ItemUtils.isExist(oldItem) && existDrop == ItemPolicy.KEEP) {
            return false; // сохраняем и уходим
        }
        armour[slot] = item;
        player.getInventory().setArmorContents(armour);
        if (oldItem != null) {
            if (existDrop == ItemPolicy.UNDRESS) {
                ItemUtils.giveItemOrDrop(player, oldItem);
            } else if (existDrop == ItemPolicy.DROP) {
                player.getWorld().dropItemNaturally(player.getLocation(), oldItem);
            }
        }
        ItemContextManager.triggerItemWear(player);
        return true;
    }

    private boolean removeItemInInventory(Environment env, Parameters params) {
        VirtualItem search = VirtualItem.fromParameters(params);
        int toRemove = search.getAmount();
        int totalRemoved = 0;
        boolean all = toRemove == -1;
        PlayerInventory inventory = env.getPlayer().getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && search.isSimilar(item)) {
                if (all) {
                    totalRemoved += item.getAmount();
                    inventory.setItem(i, null);
                } else if (item.getAmount() <= toRemove) {
                    totalRemoved += item.getAmount();
                    toRemove -= item.getAmount();
                    inventory.setItem(i, null);
                } else {
                    totalRemoved += item.getAmount() - toRemove;
                    item.setAmount(item.getAmount() - toRemove);
                    inventory.setItem(i, item);
                    break;
                }
            }
        }
        env.getVariables().set("items_removed", Integer.toString(totalRemoved));
        env.getVariables().set("item", search.toString());
        env.getVariables().set("item_str", ItemUtils.toDisplayString(params));
        return true;
    }

    private boolean removeItemInHand(Environment env, Parameters params) {
        VirtualItem search = VirtualItem.fromParameters(params);
        boolean all = !params.contains("amount");
        PlayerInventory inventory = env.getPlayer().getInventory();
        ItemStack item = inventory.getItemInMainHand();
        if (search.isSimilar(item)) {
            if (all || item.getAmount() <= search.getAmount()) {
                inventory.setItemInMainHand(null);
            } else {
                item.setAmount(item.getAmount() - search.getAmount());
                inventory.setItemInMainHand(item);
            }
        }
        if (!item.getType().isEmpty()) {
            env.getVariables().set("item", VirtualItem.asString(item));
            env.getVariables().set("item_str", ItemUtils.toDisplayString(item));
        } else {
            env.getVariables().set("item", "");
            env.getVariables().set("item_str", "");
        }
        return true;
    }

    private boolean removeItemInOffHand(Environment env, Parameters params) {
        VirtualItem search = VirtualItem.fromParameters(params);
        boolean all = !params.contains("amount");
        PlayerInventory inventory = env.getPlayer().getInventory();
        ItemStack item = inventory.getItemInOffHand();
        if (search.isSimilar(item)) {
            if (all || item.getAmount() <= search.getAmount()) {
                inventory.setItemInOffHand(null);
            } else {
                item.setAmount(item.getAmount() - search.getAmount());
                inventory.setItemInOffHand(item);
            }
        }
        if (!item.getType().isEmpty()) {
            env.getVariables().set("item", VirtualItem.asString(item));
            env.getVariables().set("item_str", ItemUtils.toDisplayString(item));
        } else {
            env.getVariables().set("item", "");
            env.getVariables().set("item_str", "");
        }
        return true;
    }

    private boolean giveItemPlayer(Environment env, final String param) {
        Player player = env.getPlayer();
        List<ItemStack> items = ItemUtils.parseRandomItemsStr(param);
        if (items.isEmpty()) return false;
        String actionItems = toDisplayString(items);
        env.getVariables().set("item_str", actionItems);
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            for (ItemStack i : items)
                ItemUtils.giveItemOrDrop(player, i);
            ItemContextManager.triggerItemHold(player);
        }, 1);
        return true;
    }

    private boolean openInventory(Environment env, String itemStr) {
        List<ItemStack> items = ItemUtils.parseRandomItemsStr(itemStr);
        if (items.isEmpty()) return false;
        String actionItems = toDisplayString(items);
        env.getVariables().set("item_str", actionItems);
        int size = Math.min(items.size(), 36);
        Inventory inv = Bukkit.createInventory(null, size);
        for (int i = 0; i < size; i++)
            inv.setItem(i, items.get(i));
        return true;
    }


    public boolean dropItems(Environment env, Parameters params) {
        Player player = env.getPlayer();
        int radius = params.getInteger("radius");
        Location loc = LocationUtils.parseLocation(params.getString("loc"), player.getLocation());
        boolean scatter = params.getBoolean("scatter", true);
        boolean land = params.getBoolean("land", true);
        List<ItemStack> items = ItemUtils.parseRandomItemsStr(params.getString("item"));
        if (items.isEmpty()) return false;
        if (radius == 0) scatter = false;
        Location l = LocationUtils.getRadiusLocation(loc, radius, land);
        for (ItemStack i : items) {
            loc.getWorld().dropItemNaturally(l, i);
            if (scatter) l = LocationUtils.getRadiusLocation(loc, radius, land);
        }
        String actionItems = toDisplayString(items);
        env.getVariables().set("item_str", actionItems);
        return true;
    }

    private boolean unwearItem(Environment env, Parameters params) {
        Player player = env.getPlayer();
        int slot = getSlotNum(params.getString("slot"));
        VirtualItem search = params.getSafe("item", VirtualItem::fromString);
        String action = params.getString("item-action", "remove"); // TODO Use enum

        ItemStack found = null;

        ItemStack[] armor = player.getInventory().getArmorContents();

        if (slot < 0) {
            for (int i = 0; i < armor.length; i++) {
                if (search.isSimilar(armor[i])) {
                    found = armor[i];
                    slot = i;
                }
            }
        } else {
            ItemStack itemSlot = armor[slot];
            if (search.isSimilar(itemSlot)) {
                found = itemSlot;
            }
        }
        if (found == null || found.getType().isEmpty()) return false;
        armor[slot] = null;
        player.getInventory().setArmorContents(armor);

        if (action.equalsIgnoreCase("drop")) {
            player.getWorld().dropItemNaturally(LocationUtils.getRadiusLocation(player.getLocation().add(0, 2, 0), 2, false), found);
        } else if (action.equalsIgnoreCase("undress") || action.equalsIgnoreCase("inventory")) {
            ItemUtils.giveItemOrDrop(player, found);
        }

        env.getVariables().set("item", VirtualItem.asString(found));
        env.getVariables().set("item_str", ItemUtils.toDisplayString(found));
        return true;
    }

    private static int getSlotNum(String slotStr) { // TODO Use enum
        return switch (slotStr.toLowerCase(Locale.ROOT)) {
            case "helmet", "helm", "head" -> 3;
            case "chestplate", "chest", "body" -> 2;
            case "leggings", "legs", "leg" -> 1;
            case "boots", "boot", "feet" -> 0;
            default -> -1;
        };
    }

    private static String toDisplayString(@NotNull List<ItemStack> items) {
        StringBuilder builder = new StringBuilder();
        for (ItemStack item : items) {
            builder.append(ItemUtils.toDisplayString(item)).append(", ");
        }
        return Utils.cutLast(builder, 2);
    }

    public enum Type {
        GIVE_ITEM,
        REMOVE_ITEM_HAND,
        REMOVE_ITEM_OFFHAND,
        REMOVE_ITEM_INVENTORY,
        DROP_ITEM,
        WEAR_ITEM,
        UNWEAR_ITEM,
        OPEN_INVENTORY,
        SET_INVENTORY,
        GET_INVENTORY
    }
}
