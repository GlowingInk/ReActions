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

package fun.reactions.module.worldedit.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldedit.external.RaWorldEdit;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

@Aliased.Names("WE_SEL_BLOCKS")
public class WeSelectionFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        int selectionBlocks = RaWorldEdit.getArea(player);
        Vector minPoint = RaWorldEdit.getMinimumPoint(player);
        Vector maxPoint = RaWorldEdit.getMaximumPoint(player);
        env.getVariables().set("minpoint", (minPoint == null) ? "" : minPoint.toString());
        env.getVariables().set("minX", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockX()));
        env.getVariables().set("minY", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockY()));
        env.getVariables().set("minZ", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockZ()));
        env.getVariables().set("maxpoint", (maxPoint == null) ? "" : maxPoint.toString());
        env.getVariables().set("maxX", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockX()));
        env.getVariables().set("maxY", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockY()));
        env.getVariables().set("maxZ", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockZ()));
        env.getVariables().set("selblocks", Integer.toString(selectionBlocks));
        OptionalInt paramsOpt = NumberUtils.parseInteger(paramsStr, Is.NON_NEGATIVE);
        return paramsOpt.isPresent() && selectionBlocks <= paramsOpt.getAsInt();
    }

    @Override
    public @NotNull String getName() {
        return "WE_SELECTION";
    }
}
