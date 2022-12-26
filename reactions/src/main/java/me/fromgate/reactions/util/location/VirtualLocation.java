package me.fromgate.reactions.util.location;

import io.papermc.paper.math.BlockPosition;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// TODO: Use it in activators

/**
 * Location class, where all the coordinates can be null
 * If coordinate is null, it will be ignored in comparison
 */
@SuppressWarnings("UnstableApiUsage")
public class VirtualLocation implements BlockPosition {
    public static final VirtualLocation EMPTY = new VirtualLocation(null, null, null, null);

    private final String world;
    private final Integer x;
    private final Integer y;
    private final Integer z;

    public VirtualLocation(@NotNull String loc) {
        String[] locSplit = loc.split(",", 5);
        world = locSplit[0];
        if (locSplit.length > 1 && NumberUtils.FLOAT.matcher(locSplit[1]).matches()) {
            x = Double.valueOf(locSplit[1]).intValue();
        } else x = null;
        if (locSplit.length > 2 && NumberUtils.FLOAT.matcher(locSplit[2]).matches()) {
            y = Double.valueOf(locSplit[2]).intValue();
        } else y = null;
        if (locSplit.length > 3 && NumberUtils.FLOAT.matcher(locSplit[3]).matches()) {
            z = Double.valueOf(locSplit[3]).intValue();
        } else z = null;
    }

    public VirtualLocation(@NotNull ConfigurationSection cfg) {
        this(
                cfg.getString("world"),
                cfg.contains("x") ? cfg.getInt("x") : null,
                cfg.contains("y") ? cfg.getInt("y") : null,
                cfg.contains("z") ? cfg.getInt("z") : null
        );
    }

    public VirtualLocation(@NotNull Parameters params) {
        this(
                params.getString("world"),
                params.contains("x") ? params.getInteger("x") : null,
                params.contains("y") ? params.getInteger("y") : null,
                params.contains("z") ? params.getInteger("z") : null
        );
    }

    public VirtualLocation(@Nullable String world, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public VirtualLocation(@NotNull Location loc) {
        this.world = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public @Nullable String worldName() {
        return this.world;
    }

    public @Nullable Integer virtualX() {
        return this.x;
    }

    public @Nullable Integer virtualY() {
        return this.y;
    }

    public @Nullable Integer virtualZ() {
        return this.z;
    }

    public int blockX(int def) {
        return x == null ? def : x;
    }

    public int blockY(int def) {
        return y == null ? def : y;
    }

    public int blockZ(int def) {
        return z == null ? def : z;
    }

    @Override
    public int blockX() {
        return x == null ? 0 : x;
    }

    @Override
    public int blockY() {
        return y == null ? 0 : y;
    }

    @Override
    public int blockZ() {
        return z == null ? 0 : z;
    }

    @Override
    public @NotNull VirtualLocation offset(int x, int y, int z) {
        return new VirtualLocation(world, this.x + x, this.y + y, this.z + z);
    }

    @Override
    public @NotNull VirtualLocation offset(@NotNull Axis axis, int amount) {
        return amount == 0 ? this : switch (axis) {
            case X -> new VirtualLocation(world, blockX(amount), y, z);
            case Y -> new VirtualLocation(world, x, blockY(amount), z);
            case Z -> new VirtualLocation(world, x, y, blockZ(amount));
        };
    }

    @Override
    public @NotNull VirtualLocation offset(@NotNull BlockFace face, int amount) {
        if (amount == 0 || face == BlockFace.SELF) return this;
        Integer modX = face.getModX() == 0 ? x : Integer.valueOf(face.getModX() * amount + blockX());
        Integer modY = face.getModY() == 0 ? y : Integer.valueOf(face.getModY() * amount + blockY());
        Integer modZ = face.getModZ() == 0 ? z : Integer.valueOf(face.getModZ() * amount + blockZ());
        return new VirtualLocation(world, modX, modY, modZ);
    }

    public @NotNull Location toLocation() {
        World world = Bukkit.getWorld(this.world);
        return toCenter().toLocation(world == null ? Bukkit.getWorlds().get(0) : world);
    }

    @Override
    public @NotNull Location toLocation(@NotNull World world) {
        Location loc = toLocation();
        loc.setWorld(world);
        return loc;
    }

    public boolean isEmpty() {
        return world == null && x == null && y == null && z == null;
    }

    public boolean isSimilar(@NotNull Location loc) {
        return isSimilar(loc.getWorld(), loc.blockX(), loc.getBlockY(), loc.blockZ());
    }

    public boolean isSimilar(@NotNull World worldOther, int xOther, int yOther, int zOther) {
        return (x == null || x == xOther) &&
                (y == null || y == yOther) &&
                (z == null || z == zOther) &&
                (world == null || worldOther.getName().equals(world));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VirtualLocation that)) return false;
        return Objects.equals(world, that.world) && Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    @Override
    public String toString() {
        return this.world + "," + x + "," + y + "," + z + ",0,0";
    }
}
