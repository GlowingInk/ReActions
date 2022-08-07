package me.fromgate.reactions.externals.worldguard;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WGBridge7x extends WGBridge {

    private static WorldGuardPlugin worldguard = null;
    private static RegionContainer container;
    private static RegionQuery query = null;

    public static boolean checkRegionInRadius(Player p, int radius) {
        if (!RaWorldEdit.isConnected()) return false;
        World world = p.getWorld();
        LocalPlayer player = RaWorldGuard.getWrapPlayer(p);
        final String id = "__canbuild__";
        com.sk89q.worldedit.util.Location loc = player.getLocation();
        BlockVector3 min = BlockVector3.at(loc.getBlockX() + radius, 0, loc.getBlockZ() + radius);
        BlockVector3 max = BlockVector3.at(loc.getBlockX() - radius, world.getMaxHeight(), loc.getBlockZ() - radius);
        ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);

        ApplicableRegionSet set = RaWorldGuard.getRegionManager(world).getApplicableRegions(region);
        if (RaWorldGuard.getRegionManager(world).overlapsUnownedRegion(region, player)) {
            for (ProtectedRegion each : set) {
                if (each != null && !each.getOwners().contains(player) && !each.getMembers().contains(player))
                    return true;
            }
        }
        return false;
    }

    public static int canBuildSelection(Player p) throws CommandException {
        boolean canBuild = false;
        World world = p.getWorld();
        LocalPlayer player = RaWorldGuard.getWrapPlayer(p);
        String id = "__canbuild__";
        ProtectedRegion region = RaWorldEdit.checkRegionFromSelection(p, id);
        if (region == null) return 3;
        ApplicableRegionSet set = RaWorldGuard.getRegionManager(world).getApplicableRegions(region);
        if (RaWorldGuard.getRegionManager(world).overlapsUnownedRegion(region, player)) {
            for (ProtectedRegion each : set) {
                if (each != null) {
                    if (!each.getOwners().contains(player) && !each.getMembers().contains(player)) {
                        return 1;
                    }
                    if (each.getOwners().contains(player) || each.getMembers().contains(player)) {
                        canBuild = true;
                    }
                }
            }
            if (!canBuild) return 1;
        } else {
            for (ProtectedRegion each : set) {
                if (each != null) {
                    if (each.getOwners().contains(player) || each.getMembers().contains(player)) {
                        BlockVector3 rgBlockMin = region.getMinimumPoint();
                        BlockVector3 rgBlockMax = region.getMaximumPoint();
                        if (each.contains(rgBlockMin.getBlockX(), rgBlockMin.getBlockY(), rgBlockMin.getBlockZ())
                                && each.contains(rgBlockMax.getBlockX(), rgBlockMax.getBlockY(), rgBlockMax.getBlockZ())) {
                            canBuild = true;
                        }
                    }
                }
            }
            if (!canBuild) return 2;
        }
        return 0;
    }

    @Override
    public void init() {
        if (!isConnected()) return;
        setVersion("WGBridge 0.0.2/WG7x");
        if (this.wgPlugin instanceof WorldGuardPlugin) {
            worldguard = (WorldGuardPlugin) wgPlugin;
            container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            query = container.createQuery();
        } else this.connected = false;
    }

    @Override
    public List<String> getRegions(Location loc) {
        List<String> rgs = new ArrayList<>();
        if (loc == null) return rgs;
        if (!connected) return rgs;
        ApplicableRegionSet rset = query.getApplicableRegions(BukkitAdapter.adapt(loc));
        if ((rset == null) || (rset.size() == 0)) return rgs;
        for (ProtectedRegion rg : rset) rgs.add((loc.getWorld().getName() + "." + rg.getId()).toLowerCase(Locale.ENGLISH));
        return rgs;
    }

    @Override
    public List<String> getRegions(Player p) {
        return getRegions(p.getLocation());
    }

    @Override
    public int countPlayersInRegion(String rg) {
        if (!connected) return 0;
        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers())
            if (isPlayerInRegion(p, rg)) count++;
        return count;
    }

    @Override
    public List<Player> playersInRegion(String rg) {
        List<Player> plrs = new ArrayList<>();
        if (!connected) return plrs;
        for (Player p : Bukkit.getOnlinePlayers())
            if (isPlayerInRegion(p, rg)) plrs.add(p);
        return plrs;
    }

    @Override
    public boolean isPlayerInRegion(Player p, String rg) {
        if (!connected) return false;
        List<String> rgs = getRegions(p);
        if (rgs.isEmpty() && !rg.contains("__global__")) return false;
        World world = getRegionWorld(rg);
        String regionName = getRegionName(rg);
        return rgs.isEmpty() && rg.contains("__global__") && p.getWorld() == world || rgs.contains((world.getName() + "." + regionName).toLowerCase(Locale.ENGLISH));
    }

    @Override
    public boolean isRegionExists(String rg) {
        if (!connected) return false;
        if (rg.isEmpty()) return false;
        World world = getRegionWorld(rg);
        String regionName = getRegionName(rg);
        return (container.get(BukkitAdapter.adapt(world)).getRegions().containsKey(regionName));
    }

    @Override
    public List<Location> getRegionMinMaxLocations(String rg) {
        List<Location> locs = new ArrayList<>();
        if (!connected) return locs;
        World world = getRegionWorld(rg);
        String regionName = getRegionName(rg);
        ProtectedRegion prg = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (prg == null) return locs;
        locs.add(new Location(world, prg.getMinimumPoint().getX(), prg.getMinimumPoint().getY(), prg.getMinimumPoint().getZ()));
        locs.add(new Location(world, prg.getMaximumPoint().getX(), prg.getMaximumPoint().getY(), prg.getMaximumPoint().getZ()));
        return locs;
    }

    @Override
    public List<Location> getRegionLocations(String rg, boolean land) {
        List<Location> locs = new ArrayList<>();
        if (!connected) return locs;
        World world = getRegionWorld(rg);
        String regionName = getRegionName(rg);
        ProtectedRegion prg = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);

        if (prg != null) {
            for (int x = prg.getMinimumPoint().getBlockX(); x <= prg.getMaximumPoint().getBlockX(); x++)
                for (int y = prg.getMinimumPoint().getBlockY(); y <= prg.getMaximumPoint().getBlockY(); y++)
                    for (int z = prg.getMinimumPoint().getBlockZ(); z <= prg.getMaximumPoint().getBlockZ(); z++) {
                        Location t = new Location(world, x, y, z);
                        if (t.getBlock().isEmpty() && t.getBlock().getRelative(BlockFace.UP).isEmpty()) {
                            if (land && t.getBlock().getRelative(BlockFace.DOWN).isEmpty()) continue;
                            t.add(0.5, 0, 0.5);
                            locs.add(t);
                        }
                    }
        }
        return locs;
    }

    @Override
    public boolean isMemberOrOwner(Player p, String region) {
        if (!connected) return false;
        LocalPlayer localPlayer = p != null ? worldguard.wrapPlayer(p) : null;
        if (localPlayer == null) return false;
        if (region.isEmpty()) return false;
        World world = getRegionWorld(region);
        String regionName = getRegionName(region);
        ProtectedRegion rg = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (rg == null) return false;
        return localPlayer.getAssociation(Collections.singletonList(rg)) != Association.NON_MEMBER;
    }


    @Override
    public boolean isOwner(Player p, String region) {
        if (!connected) return false;
        LocalPlayer localPlayer = p != null ? worldguard.wrapPlayer(p) : null;
        if (localPlayer == null) return false;
        if (region.isEmpty()) return false;
        World world = getRegionWorld(region);
        String regionName = getRegionName(region);
        ProtectedRegion rg = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (rg == null) return false;
        return localPlayer.getAssociation(Collections.singletonList(rg)) == Association.OWNER;
    }

    @Override
    public boolean isMember(Player p, String region) {
        if (!connected) return false;
        LocalPlayer localPlayer = p != null ? worldguard.wrapPlayer(p) : null;
        if (localPlayer == null) return false;
        if (region.isEmpty()) return false;
        World world = getRegionWorld(region);
        String regionName = getRegionName(region);
        ProtectedRegion rg = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (rg == null) return false;
        return localPlayer.getAssociation(Collections.singletonList(rg)) == Association.MEMBER;
    }

    @Override
    public boolean isLocationInRegion(Location loc, String region) {
        if (loc == null) return false;
        if (!connected) return false;
        World world = getRegionWorld(region);
        if (!loc.getWorld().equals(world)) return false;
        String regionName = getRegionName(region);
        ProtectedRegion rg = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (rg == null) return false;
        return (rg.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    @Override
    public boolean isFlagInRegion(Player p, String region) {
        if (!connected) return false;
        String[] parts;
        String[] group_parts = region.split("/");
        if (group_parts.length > 1) parts = group_parts[0].split("\\.");
        else parts = region.split("\\.");
        if (parts.length < 3 || parts.length > 4) return false;
        World world;
        String regionName;
        String flagName;
        String valueName;
        if (parts.length == 3) {
            world = Bukkit.getWorlds().get(0);
            regionName = parts[0];
            flagName = parts[1];
            valueName = parts[2];
        } else {
            world = Bukkit.getWorld(parts[0]);
            regionName = parts[1];
            flagName = parts[2];
            valueName = parts[3];
        }
        if (world == null) return false;
        if (flagName == null) return false;
        ProtectedRegion rg = container.get(BukkitAdapter.adapt(world)).getRegion(regionName);
        if (rg == null) return false;
        ApplicableRegionSet set = container.get(BukkitAdapter.adapt(world)).getApplicableRegions(rg);
        Flag<?> f = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
        if (f == null) return false;
        LocalPlayer localPlayer = p != null ? worldguard.wrapPlayer(p) : null;
        if (set.queryValue(localPlayer, f) == null) return false;
        boolean result = false;
        String flagStr = set.queryValue(localPlayer, f).toString();
        if (flagStr.equalsIgnoreCase(valueName)) result = true;

        if (result && group_parts.length > 1) {
            RegionGroup group;
            group = set.queryValue(null, f.getRegionGroupFlag());
            return (group.toString()).replace("_", "").equalsIgnoreCase(group_parts[1].replace("_", ""));
        } else return result;
    }

    public LocalPlayer getWrapPlayer(Player player) {
        return worldguard.wrapPlayer(player);
    }

    public RegionManager getRegionManager(World world) {
        return container.get(BukkitAdapter.adapt(world));
    }
}
