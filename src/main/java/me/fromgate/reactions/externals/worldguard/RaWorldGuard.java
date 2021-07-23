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

package me.fromgate.reactions.externals.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.module.basics.activators.RegionActivator;
import me.fromgate.reactions.module.basics.activators.RegionEnterActivator;
import me.fromgate.reactions.module.basics.activators.RegionLeaveActivator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RaWorldGuard {

    private static WGBridge bridge = null;
    private static Set<String> regionActivators = null;

    public static void init() {
        bridge = new WGBridge7x();
        updateRegionCache();
    }

    public static void updateRegionCache() {
        regionActivators = new HashSet<>();
        // TODO Custom ActivatorType
        for (Activator a : ReActions.getActivators().getType(RegionActivator.class).getActivators()) {
            RegionActivator r = (RegionActivator) a;
            regionActivators.add(r.getRegion());
        }
        for (Activator a : ReActions.getActivators().getType(RegionEnterActivator.class).getActivators()) {
            RegionEnterActivator r = (RegionEnterActivator) a;
            regionActivators.add(r.getRegion());
        }
        for (Activator a : ReActions.getActivators().getType(RegionLeaveActivator.class).getActivators()) {
            RegionLeaveActivator r = (RegionLeaveActivator) a;
            regionActivators.add(r.getRegion());
        }
    }

    public static boolean containsRegion(Location loc, String region) {
        return bridge.isLocationInRegion(loc, region);
    }

    public static Set<String> getRegions(Location loc) {
        Set<String> regions = new HashSet<>();
        for (String rg : regionActivators) {
            if (bridge.isLocationInRegion(loc, rg))
                regions.add(WGBridge7x.getFullRegionName(rg));
        }
        return regions;
    }

    public static Set<String> getRegions(Player p) {
        return getRegions(p.getLocation());
    }

    public static int countPlayersInRegion(String rg) {
        return bridge.countPlayersInRegion(rg);
    }

    public static List<Player> playersInRegion(String rg) {
        return bridge.playersInRegion(rg);
    }

    public static boolean isPlayerInRegion(Player p, String rg) {
        return bridge.isPlayerInRegion(p, rg);
    }

    public static boolean isRegionExists(String rg) {
        return bridge.isRegionExists(rg);
    }

    public static List<Location> getRegionMinMaxLocations(String rg) {
        return bridge.getRegionMinMaxLocations(rg);
    }

    public static List<Location> getRegionLocations(String rg, boolean land) {
        return bridge.getRegionLocations(rg, land);
    }

    public static boolean isMemberOrOwner(Player p, String region) {
        return bridge.isMemberOrOwner(p, region);
    }

    public static boolean isOwner(Player p, String region) {
        return bridge.isOwner(p, region);
    }

    public static boolean isMember(Player p, String region) {
        return bridge.isMember(p, region);
    }

    public static boolean isConnected() {
        return bridge != null && bridge.isConnected();
    }

    public static boolean isFlagInRegion(Player p, String region) {
        return bridge.isFlagInRegion(p, region);
    }

    public static boolean isLocationInRegion(Location loc, String regionName) {
        return bridge.isLocationInRegion(loc, regionName);
    }

    public static LocalPlayer getWrapPlayer(Player player) {
        return bridge.getWrapPlayer(player);
    }

    public static RegionManager getRegionManager(World world) {
        return bridge.getRegionManager(world);
    }
}
