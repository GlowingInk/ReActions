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

package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectionFlag implements Flag {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Player player = env.getPlayer();
        Direction d1 = Direction.getByName(content);
        if (d1 == null) return false;
        Direction d2 = Direction.getByYaw(player);
        if (d2 == null) return false;
        return (d1 == d2);
    }

    @Override
    public @NotNull String getName() {
        return "DIRECTION";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    private enum Direction {
        SOUTH,
        SOUTHWEST,
        WEST,
        NORTHWEST,
        NORTH,
        NORTHEAST,
        EAST,
        SOUTHEAST;

        private static final Map<String, Direction> BY_NAME = Stream.of(values()).collect(Collectors.toMap(Enum::name, d -> d));

        public static Direction getByName(String dirstr) {
            return BY_NAME.get(dirstr.toUpperCase(Locale.ROOT));
        }

        public static Direction getByYaw(Player p) {
            double angle = (p.getLocation().getYaw() < 0) ? (360 + p.getLocation().getYaw()) : p.getLocation().getYaw();
            int sector = (int) (angle - ((angle + 22.5) % 45.0) + 22.5);
            return switch (sector) {
                case 45 -> SOUTHWEST;
                case 90 -> WEST;
                case 135 -> NORTHWEST;
                case 180 -> NORTH;
                case 225 -> NORTHEAST;
                case 270 -> EAST;
                case 315 -> SOUTHEAST;
                default -> SOUTH;
            };
        }
    }
}
