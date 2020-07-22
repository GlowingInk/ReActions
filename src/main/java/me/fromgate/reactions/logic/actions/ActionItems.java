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

package me.fromgate.reactions.logic.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.ItemStoragesManager;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ActionItems extends Action {
    // TODO: Optimization

    private final ItemActionType actionType;

    public ActionItems(ItemActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public boolean execute(RaContext context, Parameters params) {
        switch (actionType) {
            case GIVE_ITEM:
                return giveItemPlayer(context, params.getParam("param-line", ""));
            case REMOVE_ITEM_HAND:
                return removeItemInHand(context, params);
            case REMOVE_ITEM_OFFHAND:
                return removeItemInOffHand(context, params);
            case REMOVE_ITEM_INVENTORY:
                return removeItemInInventory(context, params);
            case DROP_ITEM:
                return dropItems(context, params);
            case WEAR_ITEM:
                return wearItem(context, params);
            case OPEN_INVENTORY:
                return openInventory(context, params.getParam("param-line", ""));
            case SET_INVENTORY:
                return setInventorySlot(context, params);
            case GET_INVENTORY:
                return getInventorySlot(context, params);
            case UNWEAR_ITEM:
                return unwearItem(context, params);
        }
        return true;
    }


    /**
     * Реализует действие ITEM_SLOT - установить предмет в определенный слот
     *
     * @param context
     * @param params  - параметры: item - предмет
     *                slot - слот (Номер слота или helmet, chestplate...)
     *                exist - что делаем с уже надетым предметов (remove, undress, drop, keep)
     * @return
     */
    private boolean setInventorySlot(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        String itemStr = params.getParam("item", "");
        if (itemStr.isEmpty()) return false;
        String slotStr = params.getParam("slot", "");
        if (slotStr.isEmpty()) return false;
        if (!NumberUtils.isInteger(slotStr)) return wearItem(context, params);
        int slotNum = Integer.parseInt(slotStr);
        if (slotNum >= player.getInventory().getSize()) return false;
        String existStr = params.getParam("exist", "remove");
        ItemStack oldItem = player.getInventory().getItem(slotNum) == null ? null : player.getInventory().getItem(slotNum).clone();
        if (itemStr.equalsIgnoreCase("AIR") || itemStr.equalsIgnoreCase("NULL")) {
            player.getInventory().setItem(slotNum, null);
        } else {
            VirtualItem vi = VirtualItem.fromString(itemStr);
            if (vi == null) return false;
            player.getInventory().setItem(slotNum, vi);
        }
        if (ItemUtils.isExist(oldItem)) return true;
        if (existStr.equalsIgnoreCase("drop")) player.getWorld().dropItemNaturally(player.getLocation(), oldItem);
        else if (existStr.equalsIgnoreCase("undress")) ItemUtils.giveItemOrDrop(player, oldItem);
        else if (existStr.equalsIgnoreCase("keep")) player.getInventory().setItem(slotNum, oldItem);
        String actionItems = ItemUtils.toDisplayString(itemStr);
        setMessageParam(actionItems);
        context.setTempVariable("item_str", actionItems);

        return true;
    }

    private boolean getInventorySlot(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        String slotStr = params.getParam("slot", "");
        if (slotStr.isEmpty()) return false;
        if (!NumberUtils.isInteger(slotStr)) return wearItemView(context, params);
        int slotNum = Integer.parseInt(slotStr);
        if (slotNum >= player.getInventory().getSize()) return false;
        ItemStack item = player.getInventory().getItem(slotNum);
        String actionItems = "";
        if (item != null) actionItems = ItemUtils.itemToString(item);
        context.setTempVariable("item_str", actionItems);
        context.setTempVariable("item_str_esc", Utils.escapeJava(actionItems));

        return true;
    }

    private boolean wearItemView(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        int slot; //4 - auto, 3 - helmet, 2 - chestplate, 1 - leggins, 0 - boots
        slot = this.getSlotNum(params.getParam("slot", "auto"));
        if (slot == -1) return getItemInOffhand(context, params);
        ItemStack[] armour = player.getInventory().getArmorContents();
        ItemStack item = armour[slot];
        String actionItems = "";
        if (item != null) actionItems = ItemUtils.itemToString(item);
        context.setTempVariable("item_str", actionItems);
        context.setTempVariable("item_str_esc", Utils.escapeJava(actionItems));
        return true;
    }

    private boolean getItemInOffhand(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        String itemStr = params.getParam("slot", "");
        if (itemStr.isEmpty()) return false;
        if (!itemStr.equalsIgnoreCase("offhand")) {
            context.setTempVariable("item_str", "");
            context.setTempVariable("item_str_esc", "");
            return true;
        }
        String item = ItemUtils.itemToString(player.getInventory().getItemInOffHand());
        context.setTempVariable("item_str", item);
        context.setTempVariable("item_str_esc", Utils.escapeJava(item));
        return true;
    }


    /**
     * Реализует действие ITEM_WEAR
     *
     * @param context - контекст
     * @param params  - параметры: item - одеваемый предмет
     *                slot - слот куда одеваем (helmet, chestplate, leggins, boots, auto)
     *                exist - что делаем с уже надетым предметов (remove, undress, drop, keep)
     * @return - возвращает true если удалось нацепить предмет на игрока
     */
    private boolean wearItem(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        String itemStr = params.getParam("item", "");
        int slot = -1; //4 - auto, 3 - helmete, 2 - chestplate, 1 - leggins, 0 - boots
        int existDrop = 1; // 0 - remove, 1 - undress, 2 - drop, 3 - keep
        if (itemStr.isEmpty()) itemStr = params.getParam("param-line", "");
        else {
            slot = this.getSlotNum(params.getParam("slot", "auto"));
            String existStr = params.getParam("exist", "undress");
            if (existStr.equalsIgnoreCase("remove")) existDrop = 0;
            else if (existStr.equalsIgnoreCase("undress")) existDrop = 1;
            else if (existStr.equalsIgnoreCase("drop")) existDrop = 2;
            else if (existStr.equalsIgnoreCase("keep")) existDrop = 3;
            else existDrop = 1;
        }
        ItemStack item = null;
        if (itemStr.equalsIgnoreCase("AIR") || itemStr.equalsIgnoreCase("NULL")) {
            if (slot == -1) return setItemInOffhand(context, params, null);
            //if (slot == -1) slot = 3;
        } else {
            item = VirtualItem.fromString(itemStr);
            if (item == null) return false;
            if (slot == -1) return setItemInOffhand(context, params, item);
            // if (slot == -1) slot = getSlotByItem(item);
        }
        return setArmourItem(player, slot, item, existDrop);
    }

    private boolean setItemInOffhand(RaContext context, Parameters params, ItemStack item) {
        Player player = context.getPlayer();
        String itemStr = params.getParam("slot", "");
        if (itemStr.isEmpty()) return false;
        if (!itemStr.equalsIgnoreCase("offhand")) return false;
        player.getInventory().setItemInOffHand(item);
        ItemStoragesManager.raiseItemWearActivator(player);
        return true;
    }

    private boolean setArmourItem(Player player, int slot, ItemStack item, int existDrop) {
        ItemStack[] armour = player.getInventory().getArmorContents().clone();
        ItemStack oldItem = armour[slot] == null ? null : armour[slot].clone();
        if (ItemUtils.isExist(oldItem) && (existDrop == 3)) {
            return false; // сохраняем и уходим
        }
        armour[slot] = item;
        player.getInventory().setArmorContents(armour);
        if (oldItem != null) {
            if (existDrop == 1) {
                ItemUtils.giveItemOrDrop(player, oldItem);
            } else if (existDrop == 2) {
                player.getWorld().dropItemNaturally(player.getLocation(), oldItem);
            }
        }
        ItemStoragesManager.raiseItemWearActivator(player);
        return true;
    }

    private boolean removeItem(RaContext context, VirtualItem search, ItemStack item, boolean all) {
        if (!ItemUtils.isExist(search)) return false;
        if (search.isSimilar(item)) {
            context.setTempVariable("item", ItemUtils.itemToString(item));
            String display = ItemUtils.toDisplayString(item);
            context.setTempVariable("item_str", display);
            ItemUtils.removeItemAmount(item, all ? item.getAmount() : search.getAmount());
            setMessageParam(display);
            return true;
        }
        return false;
    }

    private boolean removeItemInHand(RaContext context, Parameters params) {
        VirtualItem search = VirtualItem.fromMap(params.getMap());
        return removeItem(context, search, context.getPlayer().getInventory().getItemInMainHand(), !params.hasAnyParam("amount"));
    }

    private boolean removeItemInOffHand(RaContext context, Parameters params) {
        VirtualItem search = VirtualItem.fromMap(params.getMap());
        return removeItem(context, search, context.getPlayer().getInventory().getItemInOffHand(), !params.hasAnyParam("amount"));
    }

    private boolean removeItemInInventory(RaContext context, Parameters params) {
        VirtualItem search = VirtualItem.fromMap(params.getMap());
        boolean all = !params.hasAnyParam("amount");
        int remCount = search.getAmount();
        for (ItemStack item : context.getPlayer().getInventory()) {
            if (removeItem(context, search, item, all) && !all) {
                remCount -= item.getAmount();
                if (remCount <= 0) break;
                search.setAmount(remCount);
            }
        }
        return true;
    }

    private boolean giveItemPlayer(RaContext context, final String param) {
        Player player = context.getPlayer();
        final List<ItemStack> items = ItemUtils.parseRandomItemsStr(param);
        if (items == null || items.isEmpty()) return false;
        String actionItems = ItemUtils.toDisplayString(items);
        setMessageParam(actionItems);
        context.setTempVariable("item_str", actionItems);
        Bukkit.getScheduler().scheduleSyncDelayedTask(ReActions.getPlugin(), () -> {
            for (ItemStack i : items)
                ItemUtils.giveItemOrDrop(player, i);
            ItemStoragesManager.raiseItemHoldActivator(player);
        }, 1);
        return true;
    }

    private boolean openInventory(RaContext context, String itemStr) {
        List<ItemStack> items = ItemUtils.parseRandomItemsStr(itemStr);
        if (items.isEmpty()) return false;
        String actionItems = ItemUtils.toDisplayString(items);
        setMessageParam(actionItems);
        context.setTempVariable("item_str", actionItems);
        int size = Math.min(items.size(), 36);
        Inventory inv = Bukkit.createInventory(null, size);
        for (int i = 0; i < size; i++)
            inv.setItem(i, items.get(i));
        return true;
    }


    public boolean dropItems(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        int radius = params.getParam("radius", 0);
        Location loc = LocationUtils.parseLocation(params.getParam("loc", ""), player.getLocation());
        if (loc == null) loc = player.getLocation();
        boolean scatter = params.getParam("scatter", true);
        boolean land = params.getParam("land", true);
        List<ItemStack> items = ItemUtils.parseRandomItemsStr(params.getParam("item", ""));
        if (items == null || items.isEmpty()) return false;
        if (radius == 0) scatter = false;
        Location l = LocationUtils.getRadiusLocation(loc, radius, land);
        for (ItemStack i : items) {
            loc.getWorld().dropItemNaturally(l, i);
            if (scatter) l = LocationUtils.getRadiusLocation(loc, radius, land);
        }
        String actionItems = ItemUtils.toDisplayString(items);
        setMessageParam(actionItems);
        context.setTempVariable("item_str", actionItems);
        return true;
    }


    /**
     * Реализует действие ITEM_UNWEAR [slot:<SlotType>|item:<Item>] [action:remove|drop|undress|
     * Если указан только слот - снимает любой предмет.
     * Если только предмет - ищет предмет
     * Если и слот и предмет - то проверяет наличие предмета в слоте.
     * <p>
     * Сохраняет плейсхолдеры: %item%, %item_str%
     *
     * @param context — контекст
     * @param params  — перечень параметров
     * @return — true - в случае успешной отработки действия
     */
    private boolean unwearItem(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        int slot = getSlotNum(params.getParam("slot"));
        String itemStr = params.getParam("item");
        String action = params.getParam("item-action", "remove");

        VirtualItem vi = null;

        ItemStack[] armor = player.getInventory().getArmorContents();

        if (slot == -1 && !itemStr.isEmpty()) {
            for (int i = 0; i < armor.length; i++) {
                if (ItemUtils.compareItemStr(armor[i], itemStr)) {
                    vi = VirtualItem.fromItemStack(armor[i]);
                    slot = i;
                }
            }
        } else if (slot >= 0) {
            ItemStack itemSlot = armor[slot];
            if (itemStr.isEmpty() || ItemUtils.compareItemStr(itemSlot, itemStr))
                vi = VirtualItem.fromItemStack(itemSlot);
        }
        if (vi == null || vi.getType() == Material.AIR) return false;
        armor[slot] = null;
        player.getInventory().setArmorContents(armor);

        if (action.equalsIgnoreCase("drop")) {
            player.getWorld().dropItemNaturally(LocationUtils.getRadiusLocation(player.getLocation().add(0, 2, 0), 2, false), vi);
        } else if (action.equalsIgnoreCase("undress") || action.equalsIgnoreCase("inventory")) {
            ItemUtils.giveItemOrDrop(player, vi);
        }

        context.setTempVariable("item", vi.toString());
        context.setTempVariable("item_str", vi.getDescription());
        return true;
    }

    private int getSlotNum(String slotStr) {
        if (slotStr.equalsIgnoreCase("helmet") || slotStr.equalsIgnoreCase("helm")) return 3;
        if (slotStr.equalsIgnoreCase("chestplate") || slotStr.equalsIgnoreCase("chest")) return 2;
        if (slotStr.equalsIgnoreCase("leggins") || slotStr.equalsIgnoreCase("leg")) return 1;
        if (slotStr.equalsIgnoreCase("boots") || slotStr.equalsIgnoreCase("boot")) return 0;
        return -1;
    }

    public enum ItemActionType {
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


	/*
		public boolean execute(RaContext context, Param params) {
		switch (actionType) {
			case 0:
				return giveItemPlayer(p, params.getParam("param-line", ""));
			case 1:
				return removeItemInHand(p, params);
			case 2:
				return removeItemInInventory(p, params);
			case 3:
				return dropItems(p, params);
			case 4:
				return wearItem(p, params);
			case 5:
				return openInventory(p, params.getParam("param-line", ""));
			case 6:
				return setInventorySlot(p, params);
			case 7:
				return unwearItem(p, params);
		}
		return true;
	}
	 */
}
