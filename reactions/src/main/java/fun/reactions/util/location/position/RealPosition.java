/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package fun.reactions.util.location.position;

import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameterizable;
import fun.reactions.util.parameter.Parameters;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;

import static fun.reactions.util.num.NumberUtils.asDouble;

@SuppressWarnings("UnstableApiUsage")
public class RealPosition implements FinePosition, Parameterizable {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.####");

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    private Parameters params;

    private RealPosition(@NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Contract(pure = true, value = "_, _, _, _, _, _ -> new")
    public static @NotNull RealPosition of(@NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        return new RealPosition(worldName, x, y, z, yaw, pitch);
    }

    @Contract(pure = true, value = "_, _, _, _ -> new")
    public static @NotNull RealPosition of(@NotNull String worldName, Position pos, float yaw, float pitch) {
        return of(worldName, pos.x(), pos.y(), pos.z(), yaw, pitch);
    }

    @Contract(pure = true, value = "_, _ -> new")
    public static @NotNull RealPosition of(@NotNull String worldName, Position pos) {
        return of(worldName, pos.x(), pos.y(), pos.z(), 0, 0);
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull RealPosition byString(@NotNull String loc) {
        String[] split = loc.split(",");
        if (split.length < 4) {
            return byLocation(getDefaultWorld(Bukkit.getServer()).getSpawnLocation());
        }
        String worldName = split[0];
        double x = asDouble(split[1]);
        double y = asDouble(split[2]);
        double z = asDouble(split[3]);
        if (split.length < 6) {
            return new RealPosition(worldName, x, y, z, 0, 0);
        } else {
            return new RealPosition(worldName, x, y, z, (float) asDouble(split[4]), (float) asDouble(split[5]));
        }
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull RealPosition byLocation(@NotNull Location loc) {
        return new RealPosition(
                loc.getWorld().getName(),
                loc.x(),
                loc.y(),
                loc.z(),
                loc.getYaw(),
                loc.getPitch()
        );
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull RealPosition fromConfiguration(@NotNull ConfigurationSection cfg) {
        return new RealPosition(
                cfg.getString("world", ""),
                cfg.getDouble("x"),
                cfg.getDouble("y"),
                cfg.getDouble("z"),
                (float) cfg.getDouble("yaw"),
                (float) cfg.getDouble("pitch")
        );
    }

    @Contract(pure = true, value = "_ -> new")
    public static @NotNull RealPosition fromParameters(@NotNull Parameters params) {
        return new RealPosition(
                params.getString("world", ""),
                params.getDouble("x"),
                params.getDouble("y"),
                params.getDouble("z"),
                (float) params.getDouble("yaw"),
                (float) params.getDouble("pitch")
        );
    }

    public void intoConfiguration(@NotNull ConfigurationSection cfg) {
        cfg.set("world", worldName);
        cfg.set("x", Double.valueOf(format(x)));
        cfg.set("y", Double.valueOf(format(y)));
        cfg.set("z", Double.valueOf(format(z)));
        cfg.set("yaw", Double.valueOf(format(yaw)));
        cfg.set("pitch", Double.valueOf(format(yaw)));
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public @NotNull Parameters asParameters() {
        if (params == null) {
            params = Parameters.fromMap(Map.of(
                    "world", worldName,
                    "x", format(x),
                    "y", format(y),
                    "z", format(z),
                    "yaw", format(yaw),
                    "pitch", format(pitch)
            ));
        }
        return params;
    }

    @Contract(pure = true)
    public @NotNull String worldName() {
        return this.worldName;
    }

    @Override
    @Contract(pure = true)
    public double x() {
        return this.x;
    }

    @Override
    @Contract(pure = true)
    public double y() {
        return this.y;
    }

    @Override
    @Contract(pure = true)
    public double z() {
        return this.z;
    }

    @Contract(pure = true)
    public float yaw() {
        return yaw;
    }

    @Contract(pure = true)
    public float pitch() {
        return pitch;
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public @NotNull RealPosition offset(int x, int y, int z) {
        return offset((double) x, y, z);
    }

    @Override
    @Contract(pure = true, value = "_, _, _ -> new")
    public @NotNull RealPosition offset(double x, double y, double z) {
        return new RealPosition(worldName, x() + x, y() + y, z() + z, yaw, pitch);
    }

    @Override
    @Contract(pure = true, value = "-> new")
    public @NotNull RealPosition toCenter() {
        return new RealPosition(worldName, blockX() + 0.5, blockY() + 0.5, blockZ() + 0.5, yaw, pitch);
    }

    @Contract(pure = true)
    public boolean isValidAt(@NotNull Location loc) {
        return isValidAt(loc.getWorld().getName(), loc.x(), loc.y(), loc.z(), loc.getYaw(), loc.getPitch());
    }

    @Contract(pure = true)
    public boolean isValidAt(@NotNull RealPosition pos) {
        return isValidAt(pos.worldName(), pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
    }

    @Contract(pure = true)
    public boolean isValidAt(@NotNull String worldName, @NotNull Position pos) {
        return isValidAt(worldName, pos.x(), pos.y(), pos.z());
    }

    @Contract(pure = true)
    public boolean isValidAt(@NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        return isValidAt(worldName, x, y, z) && imprecise(yaw(), yaw) && imprecise(pitch(), pitch);
    }

    @Contract(pure = true)
    public boolean isValidAt(@NotNull String worldName, double x, double y, double z) {
        return this.worldName.equals(worldName) && imprecise(x(), x) && imprecise(y(), y) && imprecise(z(), z);
    }

    @Contract(pure = true)
    public @NotNull Location toLocation() {
        return toLocation(Bukkit.getServer());
    }

    @Contract(pure = true)
    public @NotNull Location toLocation(@NotNull Server server) {
        World world = server.getWorld(worldName);
        if (world == null) {
            world = getDefaultWorld(server);
        }
        return toLocation(world);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Location toLocation(@NotNull World world) {
        return new Location(world, x(), y(), z(), yaw(), pitch());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Position pos) {
            if (obj instanceof RealPosition real) {
                return isValidAt(real);
            }
            return isValidAt(worldName, pos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return worldName + "," +
                format(x) + "," +
                format(y) + "," +
                format(z) + "," +
                format(yaw) + "," +
                format(pitch);
    }

    private static boolean imprecise(double a, double b) {
        return NumberUtils.equals(a, b, 1e-4);
    }

    private static @NotNull String format(double d) {
        return FORMAT.format(d);
    }

    private static @NotNull World getDefaultWorld(@NotNull Server server) {
        return server.getWorlds().get(0);
    }
}
