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

package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.location.LocationUtils;
import me.fromgate.reactions.util.location.Teleporter;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Alias("TP")
public class ActionTp implements Action {

    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        return teleportPlayer(context, params) != null;
    }

    private Location teleportPlayer(RaContext context, Parameters params) {
        Player player = context.getPlayer();
        Location loc;
        int radius = 0;
        if (params.isEmpty()) return null;
        if (params.contains("loc")) {
            loc = LocationUtils.parseLocation(params.getString("loc"), player.getLocation());
            radius = params.getInteger("radius", 0);
        } else {
            loc = LocationUtils.parseLocation(params.toString(), player.getLocation());
        }
        boolean land = params.getBoolean("land", true);

        if (loc != null) {
            if (radius > 0) loc = LocationUtils.getRadiusLocation(loc, radius, land);
            if (Cfg.centerTpCoords) {
                loc.setX(loc.getBlockX() + 0.5);
                loc.setZ(loc.getBlockZ() + 0.5);
            }
            if (!loc.getChunk().isLoaded()) loc.getChunk().load();

            context.setVariable("loc-from", LocationUtils.locationToString(player.getLocation()));
            context.setVariable("loc-from-str", LocationUtils.locationToStringFormatted(player.getLocation()));
            context.setVariable("loc-to", LocationUtils.locationToString(loc));
            context.setVariable("loc-to-str", LocationUtils.locationToStringFormatted(loc));
            Teleporter.teleport(player, loc);
            String playeffect = params.getString("effect");
            if (!playeffect.isEmpty()) {
                if (playeffect.equalsIgnoreCase("smoke") && (!params.contains("wind"))) params.put("wind", "all");
            }
        }
        return loc;
    }

    @Override
    public @NotNull String getName() {
        return "TELEPORT";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
