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
public class VelocityJumpAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Parameters params = Parameters.fromString(content);
        Player player = env.getPlayer();
        Msg.logOnce("velocity-jump-warning", "&cWarning! VELOCITY_JUMP action is under development. In next version of plugin it could be changed, renamed or removed!");
        String locStr = params.getString("loc");
        if (locStr.isEmpty()) return false;
        Location loc = LocationUtils.parseCoordinates(locStr);
        if (loc == null) return false;
        int jumpHeight = params.getInteger("jump", 5);
        Vector velocity = LocationUtils.calculateVelocity(player.getLocation(), loc, jumpHeight);
        player.setVelocity(velocity);
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "VELOCITY_JUMP";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}