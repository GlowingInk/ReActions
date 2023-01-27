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
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.BlockUtils;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("POWER")
public class ActionPowerSet implements Action {

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Location loc = LocationUtils.parseLocation(params.getString("loc"), null);
        if (loc == null) return false;
        Block b = loc.getBlock();
        if (!isPowerBlock(b)) return false;
        String state = params.getString("power", "on");
        boolean power = getPower(b, state);
        return setPower(b, power);
    }

    @Override
    public @NotNull String getName() {
        return "POWER_SET";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }


    private boolean getPower(Block b, String state) {
        boolean power = state.equalsIgnoreCase("on") || state.equalsIgnoreCase("true");
        if (state.equalsIgnoreCase("toggle")) {
            if (b.getType() == Material.LEVER) {
                Switch sw = (Switch) b.getBlockData();
                power = sw.isPowered();
            } else if (BlockUtils.isOpenable(b)) {
                power = BlockUtils.isOpen(b);
            } else power = true;
        }
        return power;
    }

    private boolean setPower(Block b, boolean power) {
        if (b.getType() == Material.LEVER) {
            Switch sw = (Switch) b.getBlockData();
            sw.setPowered(power);
            b.setBlockData(sw, true);
        } else if (BlockUtils.isOpenable(b)) {
            BlockUtils.setOpen(b, power);
        } else return false;
        return true;
    }

    private boolean isPowerBlock(Block b) {
        if (b.getType() == Material.LEVER) return true;
        return BlockUtils.isOpenable(b);
    }
}
