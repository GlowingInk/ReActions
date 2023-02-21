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

package fun.reactions.module.basics.details;

import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.logic.environment.Variable;
import fun.reactions.module.basics.activators.InventoryClickActivator;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class InventoryClickContext extends ActivationContext {
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

    public InventoryClickContext(Player p, InventoryAction action, ClickType clickType, Inventory inventory, SlotType
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

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return InventoryClickActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, Variable.property(false),
                ITEM, Variable.lazy(() -> VirtualItem.asString(item)),
                "name", Variable.simple(inventoryName),
                "click", Variable.simple(clickType),
                "action", Variable.simple(action),
                "slottype", Variable.simple(slotType),
                "inventory", Variable.simple(inventoryType),
                "key", Variable.simple(numberKey + 1),
                "itemkey", numberKey > -1 ? Variable.lazy(() -> VirtualItem.asString(getBottomInventory().getItem(numberKey))) : Variable.simple(""),
                "slot", Variable.simple(slot)
        );
    }

    public Inventory getBottomInventory() {
        return this.inventoryView.getBottomInventory();
    }

    public ItemStack getItem() {
        return this.item;
    }

    public InventoryAction getAction() {
        return this.action;
    }

    public ClickType getClickType() {
        return this.clickType;
    }

    public SlotType getSlotType() {
        return this.slotType;
    }

    public InventoryType getInventoryType() {
        return this.inventoryType;
    }

    public int getNumberKey() {
        return this.numberKey;
    }

    public int getSlot() {
        return this.slot;
    }

    public String getInventoryName() {
        return this.inventoryName;
    }
}
