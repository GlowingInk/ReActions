package fun.reactions.util.location.space;

import fun.reactions.util.location.position.ImplicitPosition;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

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

    public Cuboid(ImplicitPosition pos1, ImplicitPosition pos2) {
        this.world = pos1.worldName();
        this.xMin = Math.min(pos1.blockX(0), pos2.blockX(0));
        this.xMax = Math.max(pos1.blockX(0), pos2.blockX(0));
        this.zMin = Math.min(pos1.blockZ(0), pos2.blockZ(0));
        this.zMax = Math.max(pos1.blockZ(0), pos2.blockZ(0));
        if (pos1.virtualY() == null) {
            if (pos2.virtualY() == null) {
                this.yMin = null;
                this.yMax = null;
            } else {
                this.yMin = pos2.virtualY();
                this.yMax = pos2.virtualY();
            }
        } else {
            if (pos2.virtualY() == null) {
                this.yMin = pos1.virtualY();
                this.yMax = pos1.virtualY();
            } else {
                this.yMin = Math.min(pos1.blockY(), pos2.blockY());
                this.yMax = Math.max(pos1.blockY(), pos2.blockY());
            }
        }
    }

    @Contract(pure = true)
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

    @Contract(pure = true)
    public String world() {
        return this.world;
    }

    @Contract(pure = true)
    public int xMin() {
        return this.xMin;
    }

    @Contract(pure = true)
    public int xMax() {
        return this.xMax;
    }

    @Contract(pure = true)
    public int zMin() {
        return this.zMin;
    }

    @Contract(pure = true)
    public int zMax() {
        return this.zMax;
    }

    @Contract(pure = true)
    public @Nullable Integer yMin() {
        return this.yMin;
    }

    @Contract(pure = true)
    public @Nullable Integer yMax() {
        return this.yMax;
    }

    // TODO: toString method
}
