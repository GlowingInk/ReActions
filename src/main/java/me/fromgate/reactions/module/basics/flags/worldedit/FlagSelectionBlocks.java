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

package me.fromgate.reactions.module.basics.flags.worldedit;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FlagSelectionBlocks implements OldFlag {
    @Override
    public boolean checkFlag(RaContext context, String param) {
        Player player = context.getPlayer();
        int selectionBlocks = RaWorldEdit.getArea(player);
        Vector minPoint = RaWorldEdit.getMinimumPoint(player);
        Vector maxPoint = RaWorldEdit.getMaximumPoint(player);
        context.setVariable("minpoint", (minPoint == null) ? "" : minPoint.toString());
        context.setVariable("minX", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockX()));
        context.setVariable("minY", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockY()));
        context.setVariable("minZ", (minPoint == null) ? "" : Integer.toString(minPoint.getBlockZ()));
        context.setVariable("maxpoint", (maxPoint == null) ? "" : maxPoint.toString());
        context.setVariable("maxX", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockX()));
        context.setVariable("maxY", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockY()));
        context.setVariable("maxZ", (maxPoint == null) ? "" : Integer.toString(maxPoint.getBlockZ()));
        context.setVariable("selblocks", Integer.toString(selectionBlocks));
        return NumberUtils.isInteger(param) && selectionBlocks <= Integer.parseInt(param);
    }
}
