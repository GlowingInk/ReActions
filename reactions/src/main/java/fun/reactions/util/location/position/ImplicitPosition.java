package fun.reactions.util.location.position;

import fun.reactions.util.Utils;
import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Axis;
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
import java.util.Optional;
import java.util.OptionalInt;

import static fun.reactions.util.num.NumberUtils.asDouble;
import static org.bukkit.util.NumberConversions.floor;

/**
 * Position class, where all the coordinates can be null
 * If coordinate is null, it will be ignored in comparison
 */
@SuppressWarnings("UnstableApiUsage")
public class ImplicitPosition implements BlockPosition, Parameterizable {
    public static final ImplicitPosition EVERYWHERE = new ImplicitPosition(null, null, null, null);

    private final String worldName;
    private final Integer x;
    private final Integer y;
    private final Integer z;

    private Parameters params;

    private ImplicitPosition(@Nullable String worldName, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Contract(value = "_, _, _, _ -> new")
    public static @NotNull ImplicitPosition of(@Nullable String worldName, @Nullable Integer x, @Nullable Integer y, @Nullable Integer z) {
        return new ImplicitPosition(worldName, x, y, z);
    }

    @Contract(value = "_ -> new")
    public static @NotNull ImplicitPosition byString(@Nullable String loc) {
        if (Utils.isStringEmpty(loc)) return EVERYWHERE;
        String[] split = loc.split(",");
        if (split.length < 4) {
            return EVERYWHERE;
        }
        String worldName = split[0].equals("*") ? null : split[0];
        Integer x = split[1].equals("*") ? null : floor(asDouble(split[1]));
        Integer y = split[2].equals("*") ? null : floor(asDouble(split[2]));
        Integer z = split[3].equals("*") ? null : floor(asDouble(split[3]));
        return new ImplicitPosition(worldName, x, y, z);
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull ImplicitPosition byLocation(@Nullable Location loc) {
        if (loc == null) return EVERYWHERE;
        return new ImplicitPosition(
                loc.getWorld().getName(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ()
        );
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull ImplicitPosition fromConfiguration(@NotNull ConfigurationSection cfg) {
        return new ImplicitPosition(
                !cfg.getString("world", "*").equals("*") ? cfg.getString("world") : null,
                cfg.isInt("x") ? cfg.getInt("x") : null,
                cfg.isInt("y") ? cfg.getInt("y") : null,
                cfg.isInt("z") ? cfg.getInt("z") : null
        );
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull ImplicitPosition fromParameters(@NotNull Parameters params) {
        if (params.isEmpty()) return EVERYWHERE;
        String worldName = params.getString("world", "*");
        String x = params.getString("x", "*");
        String y = params.getString("y", "*");
        String z = params.getString("z", "*");
        return new ImplicitPosition(
                worldName.equals("*") ? null : worldName,
                x.equals("*") ? null : floor(params.getDouble("x")),
                y.equals("*") ? null : floor(params.getDouble("y")),
                z.equals("*") ? null : floor(params.getDouble("z"))
        );
    }

    public void intoConfiguration(@NotNull ConfigurationSection cfg) {
        cfg.set("world", format(worldName));
        cfg.set("x", formatNum(x));
        cfg.set("y", formatNum(y));
        cfg.set("z", formatNum(z));
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public @NotNull Parameters asParameters() {
        if (params == null) {
             params = Parameters.fromMap(Map.of(
                "world", format(worldName),
                "x", format(x),
                "y", format(y),
                "z", format(z)
            ));
        }
        return params;
    }

    @Contract(pure = true)
    public @Nullable String worldName() {
        return this.worldName;
    }

    @Contract(pure = true)
    public @Nullable Integer virtualX() {
        return this.x;
    }

    @Contract(pure = true)
    public @Nullable Integer virtualY() {
        return this.y;
    }

    @Contract(pure = true)
    public @Nullable Integer virtualZ() {
        return this.z;
    }

    @Contract(pure = true)
    public @NotNull Optional<String> optionalWorldName() {
        return Optional.ofNullable(worldName);
    }

    @Contract(pure = true)
    public @NotNull OptionalInt optionalX() {
        return x == null ? OptionalInt.empty() : OptionalInt.of(x);
    }

    @Contract(pure = true)
    public @NotNull OptionalInt optionalY() {
        return y == null ? OptionalInt.empty() : OptionalInt.of(y);
    }

    @Contract(pure = true)
    public @NotNull OptionalInt optionalZ() {
        return z == null ? OptionalInt.empty() : OptionalInt.of(z);
    }

    @Contract(pure = true)
    public int blockX(int def) {
        return x == null ? def : x;
    }

    @Contract(pure = true)
    public int blockY(int def) {
        return y == null ? def : y;
    }

    @Contract(pure = true)
    public int blockZ(int def) {
        return z == null ? def : z;
    }

    @Override
    @Contract(pure = true)
    public int blockX() {
        return blockX(0);
    }

    @Override
    @Contract(pure = true)
    public int blockY() {
        return blockY(0);
    }

    @Override
    @Contract(pure = true)
    public int blockZ() {
        return blockZ(0);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public @NotNull ImplicitPosition offset(int x, int y, int z) {
        return new ImplicitPosition(
                worldName,
                x == 0 ? this.x : Integer.valueOf(blockX() + x),
                y == 0 ? this.y : Integer.valueOf(blockY() + y),
                z == 0 ? this.z : Integer.valueOf(blockZ() + z)
        );
    }

    @Override
    @Contract(pure = true, value = "_, _ -> new")
    public @NotNull ImplicitPosition offset(@NotNull Axis axis, int amount) {
        return amount == 0 ? this : switch (axis) {
            case X -> new ImplicitPosition(worldName, blockX() + amount, y, z);
            case Y -> new ImplicitPosition(worldName, x, blockY() + amount, z);
            case Z -> new ImplicitPosition(worldName, x, y, blockZ() + amount);
        };
    }

    @Override
    @Contract(pure = true, value = "_, _-> new")
    public @NotNull ImplicitPosition offset(@NotNull BlockFace face, int amount) {
        if (amount == 0 || face == BlockFace.SELF) return this;
        Integer modX = face.getModX() == 0 ? x : Integer.valueOf(face.getModX() * amount + blockX());
        Integer modY = face.getModY() == 0 ? y : Integer.valueOf(face.getModY() * amount + blockY());
        Integer modZ = face.getModZ() == 0 ? z : Integer.valueOf(face.getModZ() * amount + blockZ());
        return new ImplicitPosition(worldName, modX, modY, modZ);
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public @NotNull BlockPosition toBlock() {
        return Position.block(blockX(), blockY(), blockZ()); // We want to discard all the custom logic at this point
    }

    @Contract(pure = true, value = "_ -> new")
    public @NotNull Location toLocation(@NotNull Server server) {
        World world;
        if (worldName == null || (world = server.getWorld(worldName)) == null) {
            world = server.getWorlds().get(0);
        }
        return toCenter().toLocation(world);
    }

    @Override
    @Contract(pure = true, value = "_ -> new")
    public @NotNull Location toLocation(@NotNull World world) {
        return toCenter().offset(0, -0.5, 0).toLocation(world);
    }

    @Contract(pure = true)
    public boolean isValidAt(@NotNull Location loc) {
        return isValidAt(loc.getWorld().getName(), loc);
    }

    @Contract(pure = true)
    public boolean isValidAt(@NotNull ImplicitPosition pos) {
        return isValidAt(pos.worldName, pos) || pos.isValidAt(worldName, this);
    }

    @Contract(pure = true)
    public boolean isValidAt(@Nullable String worldName, @NotNull Position pos) {
        return isValidAt(worldName, pos.blockX(), pos.blockY(), pos.blockZ());
    }

    @Contract(pure = true)
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
                Objects.equals(worldName, other.worldName) &&
                Objects.equals(x, other.x) &&
                Objects.equals(y, other.y) &&
                Objects.equals(z, other.z);
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

    private static @NotNull Object formatNum(@Nullable Integer num) {
        return num == null ? "*" : num;
    }
}
