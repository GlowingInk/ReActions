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

package fun.reactions.module.basics.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.module.basics.activators.ItemWearActivator;
import fun.reactions.util.item.VirtualItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemWearContext extends ActivationContext {

    public ItemWearContext(Player p) {
        super(p);
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return ItemWearActivator.class;
    }

    public boolean isItemWeared(String itemStr) { // TODO: Why is it there?
        for (ItemStack armour : player.getInventory().getArmorContents())
            if (VirtualItem.isSimilar(itemStr, armour)) return true;
        return false;
    }

}