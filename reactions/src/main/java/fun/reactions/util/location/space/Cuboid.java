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
        this.xMin = Math.min(loc1.blockX(), loc2.blockX());
        this.xMax = Math.max(loc1.blockX(), loc2.blockX());
        this.zMin = Math.min(loc1.blockZ(), loc2.blockZ());
        this.zMax = Math.max(loc1.blockZ(), loc2.blockZ());
        this.yMin = Math.min(loc1.blockY(), loc2.blockY());
        this.yMax = Math.max(loc1.blockY(), loc2.blockY());
    }

    public Cuboid(ImplicitPosition loc1, ImplicitPosition loc2) {
        this.world = loc1.worldName();
        this.xMin = Math.min(loc1.blockX(0), loc2.blockX(0));
        this.xMax = Math.max(loc1.blockX(0), loc2.blockX(0));
        this.zMin = Math.min(loc1.blockZ(0), loc2.blockZ(0));
        this.zMax = Math.max(loc1.blockZ(0), loc2.blockZ(0));
        if (loc1.virtualY() == null) {
            if (loc2.virtualY() == null) {
                this.yMin = null;
                this.yMax = null;
            } else {
                this.yMin = loc2.virtualY();
                this.yMax = loc2.virtualY();
            }
        } else {
            if (loc2.virtualY() == null) {
                this.yMin = loc1.virtualY();
                this.yMax = loc1.virtualY();
            } else {
                this.yMin = Math.min(loc1.blockY(), loc2.blockY());
                this.yMax = Math.max(loc1.blockY(), loc2.blockY());
            }
        }
    }

    @Contract(pure = true)
    public boolean isInside(Location loc, boolean head) {
        if (!loc.getWorld().getName().equalsIgnoreCase(world))
            return false;
        int x = loc.blockX();
        int z = loc.blockZ();
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
