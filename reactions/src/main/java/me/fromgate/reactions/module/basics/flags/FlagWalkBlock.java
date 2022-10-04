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

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.item.LegacyVirtualItem;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlagWalkBlock implements Flag {
    @Override
    public boolean check(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        Block walk = player.getLocation().getBlock();
        if (!walk.getType().isSolid()) walk = walk.getLocation().subtract(0, 0.1, 0).getBlock();
        // TODO: Use Parameters
        return walk.getType() == LegacyVirtualItem.fromMap(params.getMap()).getType();
    }

    @Override
    public @NotNull String getName() {
        return "WALK_BLOCK";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
