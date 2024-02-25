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

package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DirectionFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Direction d1 = Direction.getByName(paramsStr);
        if (d1 == null) return false;
        Direction d2 = Direction.getByYaw(player.getLocation());
        if (d2 == null) return false;
        return (d1 == d2);
    }

    @Override
    public @NotNull String getName() {
        return "DIRECTION";
    }

    private enum Direction {
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST,
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST;

        private static final Map<String, Direction> BY_NAME;
        static {
            Map<String, Direction> byName = new HashMap<>();
            for (Direction dir : values()) {
                String dirName = dir.name();
                byName.put(dirName, dir);
                int index = dirName.indexOf('_');
                if (index == -1) {
                    byName.put(Character.toString(dirName.charAt(0)), dir);
                } else {
                    byName.put(Character.toString(dirName.charAt(0)) + dirName.charAt(index + 1), dir);
                    byName.put(dirName.replace("_", ""), dir);
                    byName.put(dirName.replace('_', '-'), dir);
                }
            }
            BY_NAME = Map.copyOf(byName);
        }

        public static Direction getByName(String dirstr) {
            return BY_NAME.get(dirstr.toUpperCase(Locale.ROOT));
        }

        public static Direction getByYaw(Location loc) {
            double angle = (loc.getYaw() < 0) ? (360 + loc.getYaw()) : loc.getYaw();
            int sector = (int) (angle - ((angle + 22.5) % 45.0) + 22.5);
            return switch (sector) {
                case 45 -> SOUTH_WEST;
                case 90 -> WEST;
                case 135 -> NORTH_WEST;
                case 180 -> NORTH;
                case 225 -> NORTH_EAST;
                case 270 -> EAST;
                case 315 -> SOUTH_EAST;
                default -> SOUTH;
            };
        }
    }
}
