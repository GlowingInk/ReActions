package fun.reactions.module.worldedit.external;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fun.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by MaxDikiy on 9/10/2017.
 */

@Deprecated
public final class RaWorldEdit {
    private static boolean connected = false;
    private static WorldEditPlugin worldedit = null;

    private RaWorldEdit() {}

    public static boolean isConnected() {
        return connected;
    }

    public static void init() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
            worldedit = (WorldEditPlugin) plugin;
            worldedit.getWorldEdit().getEventBus().register(new WeListener());
            connected = true;
        } catch (Throwable e) {
            Msg.logMessage("WorldEdit not found...");
            connected = false;
        }
    }

    public static LocalSession getSession(Player player) {
        return worldedit.getSession(player);
    }

    public static org.bukkit.util.Vector getMinimumPoint(Player player) {
        if (isConnected()) return null;
        Region r = null;
        try {
            r = getRegion(player);
        } catch (Exception ignored) {
        }
        if (r == null) return null;
        BlockVector3 v = r.getMinimumPoint();
        return new org.bukkit.util.Vector(v.getX(), v.getY(), v.getZ());
    }

    public static org.bukkit.util.Vector getMaximumPoint(Player player) {
        if (isConnected()) return null;
        Region r = null;
        try {
            r = getRegion(player);
        } catch (Exception ignored) {
        }
        if (r == null) return null;
        BlockVector3 v = r.getMaximumPoint();
        return new org.bukkit.util.Vector(v.getX(), v.getY(), v.getZ());
    }

    public static Region getRegion(Player player) throws IncompleteRegionException {
        RegionSelector rs = getRegionSelector(player);
        if (rs == null) return null;
        return rs.getRegion();
    }

    public static RegionSelector getRegionSelector(Player player) {
        LocalSession session = worldedit.getSession(player);
        if (session == null) return null;
        return session.getRegionSelector(BukkitAdapter.adapt(player.getWorld()));
    }

    public static Region getSelection(Player player) {
        LocalSession session = worldedit.getSession(player);
        if (session == null) return null;
        try {
            return session.getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            return null;
        }
    }

    public static boolean hasSuperPickAxe(Player player) {
        return isConnected() && getSession(player).hasSuperPickAxe();
    }

    public static boolean isToolControl(Player player) {
        return isConnected() && getSession(player).isToolControlEnabled();
    }

    public static int getArea(Player player) {
        Region selection = getSelection(player);
        if (selection == null) return 0;
        return selection.getArea();
    }

    public static ProtectedRegion checkRegionFromSelection(Player player, String id) {
        Region selection = getSelection(player);
        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DRegion polySel) {
            int minY = polySel.getMaximumY();
            int maxY = polySel.getMinimumY();
            return new ProtectedPolygonalRegion(id, polySel.getPoints(), minY, maxY);
        } else if (selection instanceof CuboidRegion) {
            BlockVector3 min = selection.getMinimumPoint();
            BlockVector3 max = selection.getMaximumPoint();
            return new ProtectedCuboidRegion(id, min, max);
        } else {
            //	Bukkit.broadcastMessage("§c"+"Sorry, you can only use cuboids and polygons for WorldGuard regions.");
            return null;
        }
    }
}