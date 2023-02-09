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

package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.NumberUtils.Is;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

// TODO: WorldEdit module
@Aliased.Names("WE_SEL_BLOCKS")
public class WeSelectionFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        Player player = context.getPlayer();
        int selectionBlocks = RaWorldEdit.getArea(player);
        Vector minPoint = RaWorldEdit.getMinimumPoint(player);
        Vector maxPoint = RaWorldEdit.getMaximumPoint(player);
        context.getVariables().set("minpoint", (minPoint == null) ? "" : minPoint.toString());
        context.getVariables().set("minX", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockX()));
        context.getVariables().set("minY", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockY()));
        context.getVariables().set("minZ", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockZ()));
        context.getVariables().set("maxpoint", (maxPoint == null) ? "" : maxPoint.toString());
        context.getVariables().set("maxX", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockX()));
        context.getVariables().set("maxY", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockY()));
        context.getVariables().set("maxZ", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockZ()));
        context.getVariables().set("selblocks", Integer.toString(selectionBlocks));
        return NumberUtils.isNumber(params, Is.NATURAL) && selectionBlocks <= Integer.parseInt(params);
    }

    @Override
    public @NotNull String getName() {
        return "WE_SELECTION";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}