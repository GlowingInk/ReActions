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

import me.fromgate.reactions.data.BooleanValue;
import me.fromgate.reactions.data.DataValue;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.activators.ItemClickActivator;
import me.fromgate.reactions.util.collections.Maps;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ItemClickStorage extends Storage {

    private final EquipmentSlot hand;
    private final ItemStack item;

    public ItemClickStorage(Player p, ItemStack item, EquipmentSlot hand) {
        super(p);
        this.item = item;
        this.hand = hand;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return ItemClickActivator.class;
    }

    @Override
    protected @NotNull Map<String, String> prepareVariables() {
        Map<String, String> tempVars = new HashMap<>();
        if (item != null) {
            VirtualItem vItem = VirtualItem.fromItem(item);
            tempVars.put("item", vItem.toString());
            tempVars.put("item-str", ItemUtils.toDisplayString(vItem.asParameters()));
        }
        tempVars.put("hand", hand == EquipmentSlot.HAND ? "MAIN" : "OFF");
        return tempVars;
    }

    @Override
    protected @NotNull Map<String, DataValue> prepareChangeables() {
        return Maps.Builder.single(CANCEL_EVENT, new BooleanValue(false));
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
