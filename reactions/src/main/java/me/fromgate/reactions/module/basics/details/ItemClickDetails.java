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

package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.ItemClickActivator;
import me.fromgate.reactions.util.item.ItemUtils;
import me.fromgate.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.*;

public class ItemClickDetails extends Details {

    private final EquipmentSlot hand;
    private final ItemStack item;

    public ItemClickDetails(Player p, ItemStack item, EquipmentSlot hand) {
        super(p);
        this.item = item;
        this.hand = hand;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return ItemClickActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars = new HashMap<>();
        vars.put(CANCEL_EVENT, property(false));
        vars.put("hand", plain(hand == EquipmentSlot.HAND ? "MAIN" : "OFF"));
        if (item != null) {
            vars.put("item", lazy(() -> VirtualItem.asString(item)));
            vars.put("item-str", lazy(() -> ItemUtils.toDisplayString(item)));
        }
        return vars;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
