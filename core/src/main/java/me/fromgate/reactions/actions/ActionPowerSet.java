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

package me.fromgate.reactions.actions;

import me.fromgate.reactions.util.Locator;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.material.Lever;

public class ActionPowerSet extends Action {

    @Override
    public boolean execute(Player p, Param params) {
        Location loc = Locator.parseLocation(params.getParam("loc", ""), null);
        setMessageParam("UNKNOWN");
        if (loc == null) return false;
        Block b = loc.getBlock();
        setMessageParam(b.getType().name());
        if (!isPowerBlock(b)) return false;
        String state = params.getParam("power", "on");
        boolean power = getPower(b, state);
        return setPower(b, power);
    }


    private boolean getPower(Block b, String state) {
        boolean power = state.equalsIgnoreCase("on") || state.equalsIgnoreCase("true");
        if (state.equalsIgnoreCase("toggle")) {
            if (b.getType() == Material.LEVER) {
                Lever lever = (Lever) b.getState().getData();
                power = lever.isPowered();
            } else if (isDoorBlock(b)) {
                power = Util.isOpen(b);
            } else power = true;
        }
        return power;
    }

    private boolean setPower(Block b, boolean power) {
        if (b.getType() == Material.LEVER) {
            Switch sw = (Switch) b.getBlockData();
            sw.setPowered(power);
            b.setBlockData(sw, true);
        } else if (isDoorBlock(b)) {
            Util.setOpen(b, power);
        } else return false;
        return true;
    }

    public boolean isPowerBlock(Block b) {
        if (b.getType() == Material.LEVER) return true;
        return isDoorBlock(b);
    }

    public boolean isDoorBlock(Block b) {
        return b.getBlockData() instanceof Door;
    }


}
