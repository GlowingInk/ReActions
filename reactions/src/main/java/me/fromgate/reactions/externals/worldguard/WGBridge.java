package me.fromgate.reactions.externals.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public abstract class WGBridge {
    protected Plugin wgPlugin = null;
    protected boolean connected = false;

    private String version;

    public WGBridge() {
        connectToWorldGuard();
        setVersion("[" + this.getClass().getSimpleName() + "]");
        init();
        if (connected) {
            Msg.logMessage("WorldGuard " + wgPlugin.getDescription().getVersion() + " found. Bridge loaded: " + getVersion());
        } else Msg.logMessage("Worlguard not found...");
    }

    public static World getRegionWorld(String worldAndRegion) {
        return getRegionWorld(Bukkit.getWorlds().get(0), worldAndRegion);
    }

    public static World getRegionWorld(World w, String worldAndRegion) {
        if (!worldAndRegion.contains(".")) return w;
        String worldName = worldAndRegion.substring(0, worldAndRegion.indexOf("."));
        World world = Bukkit.getWorld(worldName);
        return world == null ? w : world;
    }

    public static String getRegionName(String worldAndRegion) {
        if (!worldAndRegion.contains(".")) return worldAndRegion;
        String regionName = worldAndRegion.substring(worldAndRegion.indexOf(".") + 1);
        return regionName.isEmpty() ? worldAndRegion : regionName;
    }

    public static String getFullRegionName(String region) {
        World world = getRegionWorld(region);
        String regionName = getRegionName(region);
        return world.getName() + "." + regionName;
    }

    public String getVersion() {
        return this.version;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    private void connectToWorldGuard() {
        Plugin twn = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (twn == null) return;
        wgPlugin = twn;
        connected = true;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public abstract void init();

    public abstract boolean isLocationInRegion(Location loc, String regionName);

    public abstract List<String> getRegions(Location loc);

    public abstract List<String> getRegions(Player p);

    public abstract int countPlayersInRegion(String rg);

    public abstract List<Player> playersInRegion(String rg);

    public abstract boolean isPlayerInRegion(Player p, String rg);

    public abstract boolean isRegionExists(String rg);

    public abstract List<Location> getRegionMinMaxLocations(String rg);

    public abstract List<Location> getRegionLocations(String rg, boolean land);

    public abstract boolean isMemberOrOwner(Player p, String region);

    public abstract boolean isOwner(Player p, String region);

    public abstract boolean isMember(Player p, String region);

    public abstract boolean isFlagInRegion(Player p, String region);

    public abstract LocalPlayer getWrapPlayer(Player player);

    public abstract RegionManager getRegionManager(World world);
}
