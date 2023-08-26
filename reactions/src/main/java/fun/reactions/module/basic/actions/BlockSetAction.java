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

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("SET_BLOCK")
public class BlockSetAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        //String istr = params.getParam("block", "");
        boolean phys = params.getBoolean("physics", false);
        boolean drop = params.getBoolean("drop", false);
        Material type = params.get("block", ItemUtils::getMaterial, Material.AIR);

        Location loc = LocationUtils.parseLocation(params.getString("loc"), null);
        if (loc == null) return false;
        Block b = loc.getBlock();

        if (drop && !b.getType().isEmpty()) b.breakNaturally();

        b.setType(type, phys);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "BLOCK_SET";
    }
}
