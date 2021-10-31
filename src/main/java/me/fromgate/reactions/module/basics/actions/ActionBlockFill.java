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

import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.item.VirtualItem;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.math.Rng;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Alias("FILL_BLOCK")
public class ActionBlockFill implements Action {

    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        boolean phys = params.getBoolean("physics", false);
        boolean drop = params.getBoolean("drop", false);
        Parameters itemParam = Parameters.fromString(params.getString("block", "AIR"), "type");
        ItemStack item = null;
        if (!itemParam.getString("type", "AIR").equalsIgnoreCase("air")) {
            item = VirtualItem.fromMap(itemParam.getMap());
            if (item == null || !item.getType().isBlock()) {
                Msg.logOnce("wrongblockfill" + params.getString("block"),
                        "Failed to execute action BLOCK_FILL. Wrong block " + params.getString("block"));
                return false;
            }
        }

        if (!params.contains("region") && !params.containsEvery("loc1", "loc2")) return false;

        Location loc1 = null;
        Location loc2 = null;

        String regionName = params.getString("region");
        if (!regionName.isEmpty()) {
            List<Location> locs = RaWorldGuard.getRegionMinMaxLocations(regionName);
            if (locs.size() == 2) {
                loc1 = locs.get(0);
                loc2 = locs.get(1);
            }
        } else {
            String locStr = params.getString("loc1");
            if (!locStr.isEmpty()) loc1 = LocationUtils.parseLocation(locStr, null);
            locStr = params.getString("loc2");
            if (!locStr.isEmpty()) loc2 = LocationUtils.parseLocation(locStr, null);
        }
        if (loc1 == null || loc2 == null) return false;

        if (!loc1.getWorld().equals(loc2.getWorld())) return false;
        int chance = params.getInteger("chance", 100);
        fillArea(item, loc1, loc2, chance, phys, drop);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "BLOCK_FILL";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private void fillArea(ItemStack blockItem, Location loc1, Location loc2, int chance, boolean phys, boolean drop) {
        Location min = new Location(loc1.getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()),
                Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
        Location max = new Location(loc1.getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()),
                Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++)
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
                    if (Rng.percentChance(chance)) {
                        Block block = min.getWorld().getBlockAt(x, y, z);
                        if (block.getType() != Material.AIR && drop) block.breakNaturally();
                        block.setType(blockItem == null ? Material.AIR : blockItem.getType(), phys);
                    }
    }
}
