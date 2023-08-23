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

package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.message.Msg;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("JUMP")
@Deprecated
public class VelocityJumpAction implements Action, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Msg.logOnce("velocity-jump-warning", "&cWarning! VELOCITY_JUMP action is under development. In next version of plugin it could be changed, renamed or removed!");
        String locStr = params.getString("loc");
        if (locStr.isEmpty()) return false;
        Location loc = LocationUtils.parseCoordinates(locStr);
        if (loc == null) return false;
        int jumpHeight = params.getInteger("jump", 5);
        Vector velocity = calculateVelocity(player.getLocation(), loc, jumpHeight);
        player.setVelocity(velocity);
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "VELOCITY_JUMP";
    }

    private static Vector calculateVelocity(Location locFrom, Location locTo, int heightGain) {
        if (!locFrom.getWorld().equals(locTo.getWorld())) return new Vector(0, 0, 0);
        // Gravity of a potion
        double gravity = 0.18; //0.115;
        Vector from = locFrom.toVector();
        Vector to = locTo.toVector();


        // Block locations
        int endGain = to.getBlockY() - from.getBlockY();
        double horizDist = from.distance(to);

        // Height gain

        //double maxGain = gain > (endGain + gain) ? gain : (endGain + gain);
        double maxGain = Math.max(heightGain, endGain + heightGain);

        // Solve quadratic equation for velocity
        double a = -horizDist * horizDist / (4 * maxGain);
        double c = -endGain;

        double slope = -horizDist / (2 * a) - Math.sqrt(horizDist * horizDist - 4 * a * c) / (2 * a);

        // Vertical velocity
        double vy = Math.sqrt(maxGain * gravity);

        // Horizontal velocity
        double vh = vy / slope;

        // Calculate horizontal direction
        int dx = to.getBlockX() - from.getBlockX();
        int dz = to.getBlockZ() - from.getBlockZ();
        double mag = Math.sqrt(dx * dx + dz * dz);
        double dirx = dx / mag;
        double dirz = dz / mag;

        // Horizontal velocity components
        double vx = vh * dirx;
        double vz = vh * dirz;

        return new Vector(vx, vy, vz);
    }
}
