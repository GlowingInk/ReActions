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

package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.ItemClickActivator;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ItemClickContext extends ActivationContext {

    private final EquipmentSlot hand;
    private final ItemStack item;

    public ItemClickContext(Player p, ItemStack item, EquipmentSlot hand) {
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
        vars.put(CANCEL_EVENT, Variable.property(false));
        vars.put("hand", Variable.simple(hand == EquipmentSlot.HAND ? "MAIN" : "OFF"));
        if (item != null) {
            vars.put("item", Variable.lazy(() -> VirtualItem.asString(item)));
            vars.put("item-str", Variable.lazy(() -> ItemUtils.toDisplayString(item)));
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
