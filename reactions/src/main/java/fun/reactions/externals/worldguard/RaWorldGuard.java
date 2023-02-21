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

package fun.reactions.externals.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import fun.reactions.ReActions;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.activators.RegionActivator;
import fun.reactions.module.basics.activators.RegionEnterActivator;
import fun.reactions.module.basics.activators.RegionLeaveActivator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class RaWorldGuard {

    private static WGBridge bridge = null;
    private static Set<String> regionActivators = null;

    public static void init() {
        bridge = new WGBridge();
        updateRegionCache();
    }

    public static void updateRegionCache() {
        regionActivators = new HashSet<>();
        // TODO Custom ActivatorType
        for (Activator a : ReActions.getActivatorTypes().get(RegionActivator.class).getActivators()) {
            RegionActivator r = (RegionActivator) a;
            regionActivators.add(r.getRegion());
        }
        for (Activator a : ReActions.getActivatorTypes().get(RegionEnterActivator.class).getActivators()) {
            RegionEnterActivator r = (RegionEnterActivator) a;
            regionActivators.add(r.getRegion());
        }
        for (Activator a : ReActions.getActivatorTypes().get(RegionLeaveActivator.class).getActivators()) {
            RegionLeaveActivator r = (RegionLeaveActivator) a;
            regionActivators.add(r.getRegion());
        }
    }

    public static Set<String> getRegions(Location loc) {
        Set<String> regions = new HashSet<>();
        for (String rg : regionActivators) {
            if (bridge.isLocationInRegion(loc, rg))
                regions.add(WGBridge.getFullRegionName(rg));
        }
        return regions;
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
