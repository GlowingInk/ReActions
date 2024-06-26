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

package fun.reactions.module.worldguard.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.util.Rng;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.DoublePredicate;

@Aliased.Names({"FILL_BLOCK", "BLOCK_FILL"})
public class BlockFillAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        if (!params.contains("region") && !params.containsEvery("loc1", "loc2")) return false;
        boolean phys = params.getBoolean("physics", false);
        boolean drop = params.getBoolean("drop", false);
        Material type = params.get("block", ItemUtils::getMaterial, Material.AIR);

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
        fillArea(type, loc1, loc2, chance, phys, drop);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "FILL_BLOCKS";
    }

    private void fillArea(Material type, Location loc1, Location loc2, int chance, boolean phys, boolean drop) {
        World world = loc1.getWorld();
        DoublePredicate check = chance >= 100 ? (d) -> true : Rng::percentChance;
        for (int x = Math.min(loc1.getBlockX(), loc2.getBlockX()), xMax = Math.max(loc1.getBlockX(), loc2.getBlockX()); x <= xMax; ++x) {
            for (int z = Math.min(loc1.getBlockZ(), loc2.getBlockZ()), zMax = Math.max(loc1.getBlockZ(), loc2.getBlockZ()); z <= zMax; ++z) {
                for (int y = Math.min(loc1.getBlockY(), loc2.getBlockY()), yMax = Math.max(loc1.getBlockY(), loc2.getBlockY()); y <= yMax; ++y) {
                    if (check.test(chance)) {
                        Block block = world.getBlockAt(x, y, z);
                        if (drop && !block.getType().isEmpty()) block.breakNaturally();
                        block.setType(type, phys);
                    }
                }
            }
        }
    }
}
