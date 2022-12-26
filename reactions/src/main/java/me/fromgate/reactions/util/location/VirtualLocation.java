package me.fromgate.reactions.util.location;

import io.papermc.paper.math.BlockPosition;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Use it in activators

/**
 * Location class, where all the coordinates can be null
 * If coordinate is null, it will be ignored in comparison
 */
@SuppressWarnings("UnstableApiUsage")
public class VirtualLocation implements BlockPosition {
    public static final VirtualLocation EMPTY = new VirtualLocation(null, null, null, null);
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(\\w+|\\*),(-?\\d+(?:\\.\\d+)?|\\*),(-?\\d+(?:\\.\\d+)?|\\*),(-?\\d+(?:\\.\\d+)?|\\*)(?:(?:,-?\\d+(?:\\.\\d+)?){2})?");

    private final String worldName;
    private final Integer x;
    private final Integer y;
    private final Integer z;

    public static VirtualLocation of(@Nullable String worldName, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z) {
        return (worldName == null && x == null && y == null && z == null) ? EMPTY : new VirtualLocation(worldName, x, y, z);
    }

    public static VirtualLocation of(@Nullable String loc) {
        if (Utils.isStringEmpty(loc)) return EMPTY;
        Matcher matcher = LOCATION_PATTERN.matcher(loc);
        if (matcher.matches()) {
            return new VirtualLocation(
                    matcher.group(1).equals("*") ? null : matcher.group(1),
                    matcher.group(2).equals("*") ? null : (int) NumberUtils.asDouble(matcher.group(2), 0),
                    matcher.group(3).equals("*") ? null : (int) NumberUtils.asDouble(matcher.group(3), 0),
                    matcher.group(4).equals("*") ? null : (int) NumberUtils.asDouble(matcher.group(4), 0)
            );
        } else {
            return EMPTY;
        }
    }

    public static VirtualLocation of(@Nullable Location loc) {
        if (loc == null) return EMPTY;
        return new VirtualLocation(
                loc.getWorld().getName(),
                loc.blockX(),
                loc.blockY(),
                loc.blockZ()
        );
    }

    public static VirtualLocation fromConfiguration(@NotNull ConfigurationSection cfg) {
        return new VirtualLocation(
                !cfg.getString("world", "*").equals("*") ? cfg.getString("world") : null,
                cfg.contains("x") ? cfg.getInt("x") : null,
                cfg.contains("y") ? cfg.getInt("y") : null,
                cfg.contains("z") ? cfg.getInt("z") : null
        );
    }

    public static VirtualLocation fromParameters(@NotNull Parameters params) {
        return new VirtualLocation(
                !params.getString("world", "*").equals("*") ? params.getString("world") : null,
                params.contains("x") ? params.getInteger("x") : null,
                params.contains("y") ? params.getInteger("y") : null,
                params.contains("z") ? params.getInteger("z") : null
        );
    }

    public VirtualLocation(@Nullable String worldName, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public @NotNull Parameters asParameters() {
        return Parameters.fromMap(Map.of(
                "world", format(worldName),
                "x", format(x),
                "y", format(y),
                "z", format(z)
        ));
    }

    public void toConfiguration(@NotNull ConfigurationSection cfg) {
        cfg.set("world", format(worldName));
        cfg.set("x", format(x));
        cfg.set("y", format(y));
        cfg.set("z", format(z));
    }

    public @Nullable String worldName() {
        return this.worldName;
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
        return new VirtualLocation(
                worldName,
                x == 0 ? this.x : Integer.valueOf(blockX(x)),
                y == 0 ? this.y : Integer.valueOf(blockY(y)),
                z == 0 ? this.z : Integer.valueOf(blockZ(z))
        );
    }

    @Override
    public @NotNull VirtualLocation offset(@NotNull Axis axis, int amount) {
        return amount == 0 ? this : switch (axis) {
            case X -> new VirtualLocation(worldName, blockX(amount), y, z);
            case Y -> new VirtualLocation(worldName, x, blockY(amount), z);
            case Z -> new VirtualLocation(worldName, x, y, blockZ(amount));
        };
    }

    @Override
    public @NotNull VirtualLocation offset(@NotNull BlockFace face, int amount) {
        if (amount == 0 || face == BlockFace.SELF) return this;
        Integer modX = face.getModX() == 0 ? x : Integer.valueOf(face.getModX() * amount + blockX());
        Integer modY = face.getModY() == 0 ? y : Integer.valueOf(face.getModY() * amount + blockY());
        Integer modZ = face.getModZ() == 0 ? z : Integer.valueOf(face.getModZ() * amount + blockZ());
        return new VirtualLocation(worldName, modX, modY, modZ);
    }

    public @NotNull Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        return toCenter().toLocation(world == null ? Bukkit.getWorlds().get(0) : world);
    }

    @Override
    public @NotNull Location toLocation(@NotNull World world) {
        Location loc = toLocation();
        loc.setWorld(world);
        return loc;
    }

    public boolean isEmpty() {
        return worldName == null && x == null && y == null && z == null;
    }

    public boolean isSimilar(@NotNull Location loc) {
        return isSimilar(loc.getWorld(), loc.blockX(), loc.getBlockY(), loc.blockZ());
    }

    public boolean isSimilar(@NotNull World worldOther, int xOther, int yOther, int zOther) {
        return (x == null || x == xOther) &&
                (y == null || y == yOther) &&
                (z == null || z == zOther) &&
                (worldName == null || worldOther.getName().equals(worldName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VirtualLocation that)) return false;
        return Objects.equals(worldName, that.worldName) && Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }

    @Override
    public String toString() {
        return format(worldName) + "," + format(x) + "," + format(y) + "," + format(z) + ",0,0";
    }

    private static String format(Object obj) {
        return obj == null ? "*" : obj.toString();
    }
}
