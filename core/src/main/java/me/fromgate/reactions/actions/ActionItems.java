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

package me.fromgate.reactions.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.storage.StorageManager;
import me.fromgate.reactions.util.Locator;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.Variables;
import me.fromgate.reactions.util.item.ItemUtil;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ActionItems extends Action {
	private ItemActionType actionType;

	public ActionItems(ItemActionType actionType) {
		this.actionType = actionType;
	}

	@Override
	public boolean execute(Player p, Param params) {
		switch (actionType) {
			case GIVE_ITEM: {
				return giveItemPlayer(p, params.getParam("param-line", ""));
			}
			case REMOVE_ITEM_HAND: {
				return removeItemInHand(p, params);
			}
			case REMOVE_ITEM_OFFHAND: {
				return removeItemInOffand(p, params);
			}
			case REMOVE_ITEM_INVENTORY: {
				return removeItemInInventory(p, params);
			}
			case DROP_ITEM: {
				return dropItems(p, params);
			}
			case WEAR_ITEM: {
				return wearItem(p, params);
			}
			case OPEN_INVENTORY: {
				return openInventory(p, params.getParam("param-line", ""));
			}
			case SET_INVENTORY: {
				return setInventorySlot(p, params);
			}
			case GET_INVENTORY: {
				return getInventorySlot(p, params);
			}
			case UNWEAR_ITEM: {
				return unwearItem(p, params);
			}
		}
		return true;
	}


	/**
	 * Реализует действие ITEM_SLOT - установить предмет в определенный слот
	 *
	 * @param player
	 * @param params - параметры: item - предмет
	 *			   slot - слот (Номер слота или helmet, chestplate...)
	 *			   exist - что делаем с уже надетым предметов (remove, undress, drop, keep)
	 * @return
	 */
	private boolean setInventorySlot(Player player, Param params) {
		String itemStr = params.getParam("item", "");
		if (itemStr.isEmpty()) return false;
		String slotStr = params.getParam("slot", "");
		if (slotStr.isEmpty()) return false;
		if (!Util.isInteger(slotStr)) return wearItem(player, params);
		int slotNum = Integer.parseInt(slotStr);
		if (slotNum >= player.getInventory().getSize()) return false;
		String existStr = params.getParam("exist", "remove");
		ItemStack oldItem = player.getInventory().getItem(slotNum) == null ? null : player.getInventory().getItem(slotNum).clone();
		if (itemStr.equalsIgnoreCase("AIR") || itemStr.equalsIgnoreCase("NULL")) {
			player.getInventory().setItem(slotNum, null);
		} else {
			VirtualItem vi = ItemUtil.itemFromString(itemStr);
			if (vi == null) return false;
			player.getInventory().setItem(slotNum, vi);
		}
		if (oldItem == null || oldItem.getType() == Material.AIR) return true;
		if (existStr.equalsIgnoreCase("drop")) player.getWorld().dropItemNaturally(player.getLocation(), oldItem);
		else if (existStr.equalsIgnoreCase("undress")) ItemUtil.giveItemOrDrop(player, oldItem);
		else if (existStr.equalsIgnoreCase("keep")) player.getInventory().setItem(slotNum, oldItem);
		String actionItems = ItemUtil.toDisplayString(itemStr);
		setMessageParam(actionItems);
		Variables.setTempVar("item_str", actionItems);

		return true;
	}

	private boolean getInventorySlot(Player player, Param params) {
		String slotStr = params.getParam("slot", "");
		if (slotStr.isEmpty()) return false;
		if (!Util.isInteger(slotStr)) return wearItemView(player, params);
		int slotNum = Integer.parseInt(slotStr);
		if (slotNum >= player.getInventory().getSize()) return false;
		ItemStack item = player.getInventory().getItem(slotNum);
		String actionItems = "";
		if (item != null) actionItems = ItemUtil.itemToString(item);
		Variables.setTempVar("item_str", actionItems);
		Variables.setTempVar("item_str_esc", Util.escapeJava(actionItems));

		return true;
	}

	private boolean wearItemView(Player player, Param params) {
		int slot; //4 - auto, 3 - helmet, 2 - chestplate, 1 - leggins, 0 - boots
		slot = this.getSlotNum(params.getParam("slot", "auto"));
		if (slot == -1) return getItemInOffhand(player, params);
		ItemStack[] armour = player.getInventory().getArmorContents();
		ItemStack item = armour[slot];
		String actionItems = "";
		if (item != null) actionItems = ItemUtil.itemToString(item);
		Variables.setTempVar("item_str", actionItems);
		Variables.setTempVar("item_str_esc", Util.escapeJava(actionItems));
		return true;
	}

	private boolean getItemInOffhand(Player player, Param params) {
		String itemStr = params.getParam("slot", "");
		if (itemStr.isEmpty()) return false;
		if (!itemStr.equalsIgnoreCase("offhand")) {
			Variables.setTempVar("item_str", "");
			Variables.setTempVar("item_str_esc", "");
			return true;
		}
		String item = ItemUtil.itemToString(player.getInventory().getItemInOffHand());
		Variables.setTempVar("item_str", item);
		Variables.setTempVar("item_str_esc", Util.escapeJava(item));
		return true;
	}


	/**
	 * Реализует действие ITEM_WEAR
	 *
	 * @param player - игрок
	 * @param params - параметры: item - одеваемый предмет
	 *			   slot - слот куда одеваем (helmet, chestplate, leggins, boots, auto)
	 *			   exist - что делаем с уже надетым предметов (remove, undress, drop, keep)
	 * @return - возвращает true если удалось нацепить предмет на игрока
	 */
	private boolean wearItem(Player player, Param params) {
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
			if (slot == -1) return setItemInOffhand(player, params, null);
			//if (slot == -1) slot = 3;
		} else {
			item = ItemUtil.parseItemStack(itemStr);
			if (item == null) return false;
			if (slot == -1) return setItemInOffhand(player, params, item);
			// if (slot == -1) slot = getSlotByItem(item);
		}
		return setArmourItem(player, slot, item, existDrop);
	}

	private boolean setItemInOffhand(Player player, Param params, ItemStack item) {
		String itemStr = params.getParam("slot", "");
		if (itemStr.isEmpty()) return false;
		if (!itemStr.equalsIgnoreCase("offhand")) return false;
		player.getInventory().setItemInOffHand(item);
		StorageManager.raiseItemWearEvent(player);
		return true;
	}

	private boolean setArmourItem(Player player, int slot, ItemStack item, int existDrop) {
		ItemStack[] armour = player.getInventory().getArmorContents().clone();
		ItemStack oldItem = armour[slot] == null ? null : armour[slot].clone();
		if (oldItem != null && oldItem.getType() != Material.AIR && (existDrop == 3)) {
			return false; // сохраняем и уходим
		}
		armour[slot] = item;
		player.getInventory().setArmorContents(armour);
		if (oldItem != null) {
			if (existDrop == 1) {
				ItemUtil.giveItemOrDrop(player, oldItem);
			} else if (existDrop == 2) {
				player.getWorld().dropItemNaturally(player.getLocation(), oldItem);
			}
		}
		StorageManager.raiseItemWearEvent(player);
		return true;
	}


	private int getSlotByItem(ItemStack item) {
		switch (item.getType()) {
			case LEATHER_BOOTS:
			case CHAINMAIL_BOOTS:
			case IRON_BOOTS:
			case GOLDEN_BOOTS:
			case DIAMOND_BOOTS:
				return 0;
			case LEATHER_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case IRON_LEGGINGS:
			case GOLDEN_LEGGINGS:
			case DIAMOND_LEGGINGS:
				return 1;
			case LEATHER_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
			case IRON_CHESTPLATE:
			case GOLDEN_CHESTPLATE:
			case DIAMOND_CHESTPLATE:
				return 2;
			/*
			case LEATHER_HELMET:
			case CHAINMAIL_HELMET:
			case IRON_HELMET:
			case GOLDEN_HELMET:
			case DIAMOND_HELMET:
			case PUMPKIN:
			*/
			default:
				return 3;
		}
	}


	private boolean removeItemInHand(Player player, Param params) {
		String itemStr = params.getParam("param-line", "");
		if (itemStr.isEmpty()) return false;
		Variables.setTempVar("item", ItemUtil.itemToString(player.getInventory().getItemInMainHand()));
		if (!ItemUtil.removeItemInHand(player, itemStr)) return false;
		String actionItems = ItemUtil.toDisplayString(itemStr);
		setMessageParam(actionItems);
		Variables.setTempVar("item_str", actionItems);

		return true;
	}

	private boolean removeItemInOffand(Player player, Param params) {
		String itemStr = params.getParam("param-line", "");
		if (itemStr.isEmpty()) return false;
		Variables.setTempVar("item", ItemUtil.itemToString(player.getInventory().getItemInOffHand()));
		if (!ItemUtil.removeItemInOffHand(player, itemStr)) return false;
		String actionItems = ItemUtil.toDisplayString(itemStr);
		setMessageParam(actionItems);
		Variables.setTempVar("item_str", actionItems);
		return true;
	}

	private boolean removeItemInInventory(Player p, Param params) {
		String itemStr = params.getParam("param-line", "");
		if (itemStr.isEmpty()) return false;
		ItemUtil.removeItemInInventory(p, itemStr);
		String actionItems = ItemUtil.toDisplayString(itemStr);
		setMessageParam(actionItems);
		Variables.setTempVar("item_str", actionItems);
		VirtualItem vi = ItemUtil.itemFromString(itemStr);
		if (vi != null) Variables.setTempVar("item", vi.toString());
		return true;
	}

	private boolean giveItemPlayer(final Player p, final String param) {
		final List<ItemStack> items = ItemUtil.parseRandomItemsStr(param);
		if (items == null || items.isEmpty()) return false;
		String actionItems = ItemUtil.toDisplayString(items);
		setMessageParam(actionItems);
		Variables.setTempVar("item_str", actionItems);
		Bukkit.getScheduler().scheduleSyncDelayedTask(ReActions.getPlugin(), () -> {
			for (ItemStack i : items)
				ItemUtil.giveItemOrDrop(p, i);
			StorageManager.raiseItemHoldEvent(p);
		}, 1);
		return true;
	}

	private boolean openInventory(Player p, String itemStr) {
		List<ItemStack> items = ItemUtil.parseRandomItemsStr(itemStr);
		if (items.isEmpty()) return false;
		String actionItems = ItemUtil.toDisplayString(items);
		setMessageParam(actionItems);
		Variables.setTempVar("item_str", actionItems);
		int size = Math.min(items.size(), 36);
		Inventory inv = Bukkit.createInventory(null, size);
		for (int i = 0; i < size; i++)
			inv.setItem(i, items.get(i));
		return true;
	}


	public boolean dropItems(Player p, Param params) {
		int radius = params.getParam("radius", 0);
		Location loc = Locator.parseLocation(params.getParam("loc", ""), p.getLocation());
		if (loc == null) loc = p.getLocation();
		boolean scatter = params.getParam("scatter", true);
		boolean land = params.getParam("land", true);
		List<ItemStack> items = ItemUtil.parseRandomItemsStr(params.getParam("item", ""));
		if (items == null || items.isEmpty()) return false;
		if (radius == 0) scatter = false;
		Location l = Locator.getRadiusLocation(loc, radius, land);
		for (ItemStack i : items) {
			loc.getWorld().dropItemNaturally(l, i);
			if (scatter) l = Locator.getRadiusLocation(loc, radius, land);
		}
		String actionItems = ItemUtil.toDisplayString(items);
		setMessageParam(actionItems);
		Variables.setTempVar("item_str", actionItems);
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
	 * @param player — игрок
	 * @param params — перечень параметров
	 * @return — true - в случае успешной отработки действия
	 */
	private boolean unwearItem(Player player, Param params) {
		int slot = getSlotNum(params.getParam("slot"));
		String itemStr = params.getParam("item");
		String action = params.getParam("item-action", "remove");

		VirtualItem vi = null;

		ItemStack[] armor = player.getInventory().getArmorContents();

		if (slot == -1 && !itemStr.isEmpty()) {
			for (int i = 0; i < armor.length; i++) {
				if (ItemUtil.compareItemStr(armor[i], itemStr)) {
					vi = ItemUtil.itemFromItemStack(armor[i]);
					slot = i;
				}
			}
		} else if (slot >= 0) {
			ItemStack itemSlot = armor[slot];
			if (itemStr.isEmpty() || ItemUtil.compareItemStr(itemSlot, itemStr))
				vi = ItemUtil.itemFromItemStack(itemSlot);
		}
		if (vi == null || vi.getType() == Material.AIR) return false;
		armor[slot] = null;
		player.getInventory().setArmorContents(armor);

		if (action.equalsIgnoreCase("drop")) {
			player.getWorld().dropItemNaturally(Locator.getRadiusLocation(player.getLocation().add(0, 2, 0), 2, false), vi);
		} else if (action.equalsIgnoreCase("undress") || action.equalsIgnoreCase("inventory")) {
			ItemUtil.giveItemOrDrop(player, vi);
		}

		Variables.setTempVar("item", vi.toString());
		Variables.setTempVar("item_str", vi.getDescription());
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
		public boolean execute(Player p, Param params) {
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
