package fun.reactions.util.location;

import org.bukkit.Location;

public class Cuboid {

    private final String world;
    private final int xMin;
    private final int xMax;
    private final int zMin;
    private final int zMax;
    private final Integer yMin;
    private final Integer yMax;

    public Cuboid(Location loc1, Location loc2) {
        this.world = loc1.getWorld().getName();
        this.xMin = Math.min(loc1.getBlockX(), loc2.getBlockX());
        this.xMax = Math.max(loc1.getBlockX(), loc2.getBlockX());
        this.zMin = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        this.zMax = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        this.yMin = Math.min(loc1.getBlockY(), loc2.getBlockY());
        this.yMax = Math.max(loc1.getBlockY(), loc2.getBlockY());
    }

    public Cuboid(ImplicitPosition loc1, ImplicitPosition loc2) {
        this.world = loc1.worldName();
        this.xMin = Math.min(loc1.blockX(0), loc2.blockX(0));
        this.xMax = Math.max(loc1.blockX(0), loc2.blockX(0));
        this.zMin = Math.min(loc1.blockZ(0), loc2.blockZ(0));
        this.zMax = Math.max(loc1.blockZ(0), loc2.blockZ(0));
        if (loc1.virtualY() == null || loc2.virtualY() == null) {
            yMin = null;
            yMax = null;
        } else {
            this.yMin = Math.min(loc1.virtualY(), loc2.virtualY());
            this.yMax = Math.max(loc1.virtualY(), loc2.virtualY());
        }
    }

    public boolean isInside(Location loc, boolean head) {
        if (!loc.getWorld().getName().equalsIgnoreCase(world))
            return false;
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        if ((xMin > x || xMax < x) || (zMin > z || zMax < z))
            return false;
        if (yMin == null)
            return true;
        double y = loc.getY();
        return (y >= yMin && y <= yMax) || (head && (y + 1.75 >= yMin && y + 1.75 <= yMax));
    }

    public String getWorld() {
        return this.world;
    }

    public int getXMin() {
        return this.xMin;
    }

    public int getXMax() {
        return this.xMax;
    }

    public int getZMin() {
        return this.zMin;
    }

    public int getZMax() {
        return this.zMax;
    }

    public Integer getYMin() {
        return this.yMin;
    }

    public Integer getYMax() {
        return this.yMax;
    }

    // TODO: toString method
}
