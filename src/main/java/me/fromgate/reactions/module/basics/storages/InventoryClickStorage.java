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

package me.fromgate.reactions.module.basics.storages;

import lombok.Getter;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.InventoryClickActivator;
import me.fromgate.reactions.util.collections.MapBuilder;
import me.fromgate.reactions.util.data.BooleanValue;
import me.fromgate.reactions.util.data.DataValue;
import me.fromgate.reactions.util.data.ItemStackValue;
import me.fromgate.reactions.util.item.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@Getter
public class InventoryClickStorage extends Storage {
    public static final String ITEM = "item";

    private final ItemStack item;
    private final InventoryAction action;
    private final ClickType clickType;
    private final SlotType slotType;
    private final InventoryType inventoryType;
    private final int numberKey;
    private final int slot;
    private final String inventoryName;
    private final InventoryView inventoryView;

    public InventoryClickStorage(Player p, InventoryAction action, ClickType clickType, Inventory inventory, SlotType
            slotType, ItemStack item, int numberKey, InventoryView inventoryView, int slot) {
        super(p);
        this.inventoryName = inventoryView.getTitle();
        this.action = action;
        this.clickType = clickType;
        this.inventoryType = inventory.getType();
        this.slotType = slotType;
        this.item = item;
        this.numberKey = numberKey;
        this.slot = slot;
        this.inventoryView = inventoryView;
    }

    public Inventory getBottomInventory() {
        return this.inventoryView.getBottomInventory();
    }

    @Override
    public Class<? extends Activator> getType() {
        return InventoryClickActivator.class;
    }

    @Override
    protected Map<String, String> prepareVariables() {
        return new MapBuilder<String, String>()
                .put("name", inventoryName)
                .put("click", clickType.name())
                .put("action", action.name())
                .put("slotType", slotType.name())
                .put("inventory", inventoryType.name())
                .put("item", ItemUtils.itemToString(item))
                .put("key", Integer.toString(numberKey + 1))
                .put("itemkey", (numberKey > -1) ? ItemUtils.itemToString(getBottomInventory().getItem(numberKey)) : "")
                .put("slot", Integer.toString(slot))
                .build();
    }

    @Override
    protected Map<String, DataValue> prepareChangeables() {
        return new MapBuilder<String, DataValue>()
                .put(CANCEL_EVENT, new BooleanValue(false))
                .put(ITEM, new ItemStackValue(item))
                .build();
    }
}
