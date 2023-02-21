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

package fun.reactions.util.location;

import fun.reactions.util.NumberUtils;
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
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fun.reactions.util.NumberUtils.asDouble;
import static fun.reactions.util.NumberUtils.isNumber;

@SuppressWarnings("UnstableApiUsage")
public class RealPosition implements FinePosition, Parameterizable { // TODO Many similarities with ImplicitPosition
    private static final Pattern POS_PATTERN = Pattern.compile( // world,x,y,z,yaw,pitch
            "(\\w+)" +
            ",(" + NumberUtils.FLOAT + ")" +
            ",(" + NumberUtils.FLOAT + ")" +
            ",(" + NumberUtils.FLOAT + ")" +
            "(?:,(" + NumberUtils.FLOAT + "),(" + NumberUtils.FLOAT + "))?"
    );
    private static final DecimalFormat FORMAT = new DecimalFormat("#.####");

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final Rotation rotation;

    private RealPosition(@NotNull String worldName, double x, double y, double z, @Nullable Rotation rotation) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }

    public static @NotNull RealPosition of(@NotNull String worldName, double x, double y, double z, @Nullable Float yaw, @Nullable Float pitch) {
        return new RealPosition(worldName, x, y, z, (yaw != null && pitch != null ? new Rotation(yaw, pitch) : null));
    }

    public static @NotNull RealPosition of(@NotNull String loc) {
        Matcher matcher = POS_PATTERN.matcher(loc);
        if (matcher.matches()) {
            return new RealPosition(
                    matcher.group(1),
                    asDouble(matcher.group(2), 0),
                    asDouble(matcher.group(3), 0),
                    asDouble(matcher.group(4), 0),
                    (isNumber(matcher.group(5)) && isNumber(matcher.group(6))
                            ? new Rotation((float) asDouble(matcher.group(5), 0), (float) asDouble(matcher.group(6), 0))
                            : null
                    )
            );
        } else {
            return of(getDefaultWorld().getSpawnLocation());
        }
    }

    public static @NotNull RealPosition of(@NotNull Location loc) {
        return new RealPosition(
                loc.getWorld().getName(),
                loc.x(),
                loc.y(),
                loc.z(),
                new Rotation(loc.getYaw(), loc.getPitch())
        );
    }

    public static @NotNull RealPosition fromConfiguration(@NotNull ConfigurationSection cfg) {
        return new RealPosition(
                cfg.getString("world", ""),
                cfg.getDouble("x"),
                cfg.getDouble("y"),
                cfg.getDouble("z"),
                (cfg.isDouble("yaw") && cfg.isDouble("pitch")
                        ? new Rotation((float) cfg.getDouble("yaw"), (float) cfg.getDouble("pitch"))
                        : null
                )
        );
    }

    public static @NotNull RealPosition fromParameters(@NotNull Parameters params) {
        return new RealPosition(
                params.getString("world", ""),
                params.getDouble("x"),
                params.getDouble("y"),
                params.getDouble("z"),
                (params.contains("yaw") && params.contains("pitch")
                        ? new Rotation((float) params.getDouble("yaw"), (float) params.getDouble("pitch"))
                        : null
                )
        );
    }

    public void intoConfiguration(@NotNull ConfigurationSection cfg) {
        cfg.set("world", worldName);
        cfg.set("x", Double.valueOf(format(x)));
        cfg.set("y", Double.valueOf(format(y)));
        cfg.set("z", Double.valueOf(format(z)));
        if (rotation != null) {
            cfg.set("yaw", Double.valueOf(format(rotation.yaw())));
            cfg.set("pitch", Double.valueOf(format(rotation.pitch())));
        }
    }

    @Override
    public @NotNull Parameters asParameters() {
        if (rotation == null) {
            return Parameters.fromMap(Map.of(
                    "world", worldName,
                    "x", format(x),
                    "y", format(y),
                    "z", format(z)
            ));
        } else {
            return Parameters.fromMap(Map.of(
                    "world", worldName,
                    "x", format(x),
                    "y", format(y),
                    "z", format(z),
                    "yaw", format(rotation.yaw()),
                    "pitch", format(rotation.pitch())
            ));
        }
    }

    public @NotNull String worldName() {
        return this.worldName;
    }

    @Override
    public double x() {
        return this.x;
    }

    @Override
    public double y() {
        return this.y;
    }

    @Override
    public double z() {
        return this.z;
    }

    public @Nullable Float yaw() {
        return rotation == null ? null : rotation.yaw();
    }

    public @Nullable Float pitch() {
        return rotation == null ? null : rotation.pitch();
    }

    public float yaw(float def) {
        return rotation == null ? def : rotation.yaw();
    }

    public float pitch(float def) {
        return rotation == null ? def : rotation.pitch();
    }

    @Override
    public @NotNull RealPosition offset(int x, int y, int z) {
        return offset((double) x, y, z);
    }

    @Override
    public @NotNull RealPosition offset(double x, double y, double z) {
        return new RealPosition(worldName, x() + x, y() + y, z() + z, rotation);
    }

    @Override
    public @NotNull RealPosition toCenter() {
        return new RealPosition(worldName, blockX() + 0.5, blockY() + 0.5, blockZ() + 0.5, rotation);
    }

    public boolean isValidAt(@NotNull Location loc) {
        return isValidAt(loc.getWorld().getName(), loc.x(), loc.y(), loc.z(), loc.getYaw(), loc.getPitch());
    }

    public boolean isValidAt(@NotNull RealPosition pos) {
        return isValidAt(pos.worldName(), pos.x(), pos.y(), pos.z()) && Objects.equals(rotation, pos.rotation);
    }

    public boolean isValidAt(@NotNull String worldName, @NotNull Position pos) {
        return isValidAt(worldName, pos.x(), pos.y(), pos.z());
    }

    public boolean isValidAt(@NotNull String worldName, double x, double y, double z) {
        return this.worldName.equals(worldName) && imprecise(x(), x) && imprecise(y(), y) && imprecise(z(), z);
    }

    public boolean isValidAt(@NotNull String worldName, double x, double y, double z, float yaw, float pitch) {
        return isValidAt(worldName, x, y, z) && (rotation == null || imprecise(rotation.yaw(), yaw) && imprecise(rotation.pitch(), pitch));
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
    public @NotNull Location toLocation(@NotNull World world) {
        return new Location(world, x(), y(), z(), yaw(0), pitch(0));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof RealPosition other && isValidAt(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z, rotation);
    }

    @Override
    public String toString() {
        return worldName + "," +
                format(x) + "," +
                format(y) + "," +
                format(z) +
                (rotation == null
                        ? ""
                        : "," + format(rotation.yaw()) + "," + format(rotation.pitch())
                );
    }

    private static boolean imprecise(double a, double b) {
        return NumberUtils.equals(a, b, 1e-4);
    }

    private static @NotNull String format(double d) {
        return FORMAT.format(d);
    }

    private static @NotNull World getDefaultWorld() {
        return getDefaultWorld(Bukkit.getServer());
    }

    private static @NotNull World getDefaultWorld(@NotNull Server server) {
        return server.getWorlds().get(0);
    }

    private record Rotation(float yaw, float pitch) {}
}
