package me.fromgate.reactions.util.location;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.parameter.Parameterizable;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Use it in activators

/**
 * Position class, where all the coordinates can be null
 * If coordinate is null, it will be ignored in comparison
 */
@SuppressWarnings("UnstableApiUsage")
public class ImplicitPosition implements BlockPosition, Parameterizable {
    public static final ImplicitPosition EMPTY = new ImplicitPosition(null, null, null, null);
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(\\w+|\\*),(-?\\d+(?:\\.\\d+)?|\\*),(-?\\d+(?:\\.\\d+)?|\\*),(-?\\d+(?:\\.\\d+)?|\\*)(?:(?:,-?\\d+(?:\\.\\d+)?){2})?");

    private final String worldName;
    private final Integer x;
    private final Integer y;
    private final Integer z;

    private ImplicitPosition(@Nullable String worldName, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static @NotNull ImplicitPosition of(@Nullable String worldName, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z) {
        return (worldName == null && x == null && y == null && z == null) ? EMPTY : new ImplicitPosition(worldName, x, y, z);
    }

    public static @NotNull ImplicitPosition of(@Nullable String loc) {
        if (Utils.isStringEmpty(loc)) return EMPTY;
        Matcher matcher = LOCATION_PATTERN.matcher(loc);
        if (matcher.matches()) {
            return new ImplicitPosition(
                    matcher.group(1).equals("*") ? null : matcher.group(1),
                    matcher.group(2).equals("*") ? null : (int) NumberUtils.asDouble(matcher.group(2), 0),
                    matcher.group(3).equals("*") ? null : (int) NumberUtils.asDouble(matcher.group(3), 0),
                    matcher.group(4).equals("*") ? null : (int) NumberUtils.asDouble(matcher.group(4), 0)
            );
        } else {
            return EMPTY;
        }
    }

    public static @NotNull ImplicitPosition of(@Nullable Location loc) {
        if (loc == null) return EMPTY;
        return new ImplicitPosition(
                loc.getWorld().getName(),
                loc.blockX(),
                loc.blockY(),
                loc.blockZ()
        );
    }

    public static @NotNull ImplicitPosition fromConfiguration(@NotNull ConfigurationSection cfg) {
        return new ImplicitPosition(
                !cfg.getString("world", "*").equals("*") ? cfg.getString("world") : null,
                cfg.contains("x") ? cfg.getInt("x") : null,
                cfg.contains("y") ? cfg.getInt("y") : null,
                cfg.contains("z") ? cfg.getInt("z") : null
        );
    }

    public static @NotNull ImplicitPosition fromParameters(@NotNull Parameters params) {
        return new ImplicitPosition(
                !params.getString("world", "*").equals("*") ? params.getString("world") : null,
                params.contains("x") ? params.getInteger("x") : null,
                params.contains("y") ? params.getInteger("y") : null,
                params.contains("z") ? params.getInteger("z") : null
        );
    }

    @Override
    public @NotNull Parameters asParameters() {
        return Parameters.fromMap(Map.of(
                "world", format(worldName),
                "x", format(x),
                "y", format(y),
                "z", format(z)
        ));
    }

    public void intoConfiguration(@NotNull ConfigurationSection cfg) {
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
        return blockX(0);
    }

    @Override
    public int blockY() {
        return blockY(0);
    }

    @Override
    public int blockZ() {
        return blockZ(0);
    }

    @Override
    @Contract(pure = true)
    public @NotNull ImplicitPosition offset(int x, int y, int z) {
        return new ImplicitPosition(
                worldName,
                x == 0 ? this.x : Integer.valueOf(blockX(x)),
                y == 0 ? this.y : Integer.valueOf(blockY(y)),
                z == 0 ? this.z : Integer.valueOf(blockZ(z))
        );
    }

    @Override
    @Contract(pure = true)
    public @NotNull ImplicitPosition offset(@NotNull Axis axis, int amount) {
        return amount == 0 ? this : switch (axis) {
            case X -> new ImplicitPosition(worldName, blockX(amount), y, z);
            case Y -> new ImplicitPosition(worldName, x, blockY(amount), z);
            case Z -> new ImplicitPosition(worldName, x, y, blockZ(amount));
        };
    }

    @Override
    @Contract(pure = true)
    public @NotNull ImplicitPosition offset(@NotNull BlockFace face, int amount) {
        if (amount == 0 || face == BlockFace.SELF) return this;
        Integer modX = face.getModX() == 0 ? x : Integer.valueOf(face.getModX() * amount + blockX());
        Integer modY = face.getModY() == 0 ? y : Integer.valueOf(face.getModY() * amount + blockY());
        Integer modZ = face.getModZ() == 0 ? z : Integer.valueOf(face.getModZ() * amount + blockZ());
        return new ImplicitPosition(worldName, modX, modY, modZ);
    }

    @Override
    @Contract(pure = true)
    public @NotNull BlockPosition toBlock() {
        return Position.block(blockX(), blockY(), blockZ()); // We want to discard all the custom logic at this point
    }

    @Contract(pure = true)
    public @NotNull Location toLocation() {
        return toLocation(Bukkit.getServer());
    }

    @Contract(pure = true)
    public @NotNull Location toLocation(@NotNull Server server) {
        World world;
        if (worldName == null || (world = server.getWorld(worldName)) == null) {
            world = server.getWorlds().get(0);
        }
        return toCenter().toLocation(world);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Location toLocation(@NotNull World world) {
        return toCenter().toLocation(world);
    }

    public boolean isValidAt(@NotNull Location loc) {
        return isValidAt(loc.getWorld().getName(), loc);
    }

    public boolean isValidAt(@NotNull ImplicitPosition pos) {
        return isValidAt(pos.worldName, pos) || pos.isValidAt(worldName, this);
    }

    public boolean isValidAt(@Nullable String worldName, @NotNull Position pos) {
        return isValidAt(worldName, pos.blockX(), pos.blockY(), pos.blockZ());
    }

    public boolean isValidAt(@Nullable String worldOther, int xOther, int yOther, int zOther) {
        return (x == null || x == xOther) &&
                (y == null || y == yOther) &&
                (z == null || z == zOther) &&
                (worldName == null || worldName.equals(worldOther));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof ImplicitPosition other &&
                Objects.equals(x, other.x) &&
                Objects.equals(y, other.y) &&
                Objects.equals(z, other.z) &&
                Objects.equals(worldName, other.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }

    @Override
    public String toString() {
        return format(worldName) + "," + format(x) + "," + format(y) + "," + format(z) + ",0,0";
    }

    private static @NotNull String format(@Nullable Object obj) {
        return obj == null ? "*" : obj.toString();
    }
}
