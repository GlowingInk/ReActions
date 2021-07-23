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

package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Alias("SET_BLOCK")
public class ActionBlockSet extends Action {

    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        //String istr = params.getParam("block", "");
        boolean phys = params.getBoolean("physics", false);
        boolean drop = params.getBoolean("drop", false);
        Parameters itemParam = Parameters.fromString(params.getString("block", "AIR"), "type");
        ItemStack item = null;
        if (!itemParam.getString("type", "AIR").equalsIgnoreCase("air")) {
            item = VirtualItem.fromMap(itemParam.getMap());
            if ((item == null) || ((!item.getType().isBlock()))) {
                Msg.logOnce("wrongblock" + params.getString("block"), "Failed to execute action BLOCK_FILL. Wrong block " + params.getString("block"));
                return false;
            }
        }

        Location loc = LocationUtils.parseLocation(params.getString("loc", ""), null);
        if (loc == null) return false;
        Block b = loc.getBlock();

        if (b.getType() != Material.AIR && drop) b.breakNaturally();

        if (item != null) {
            b.setType(item.getType());
            //b.setBlockData(item.getData(),phys);
            //b.setTypeIdAndData(item.getTypeId(), item.getData().getData(), phys);
        } else b.setType(Material.AIR, phys);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "BLOCK_SET";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

}
