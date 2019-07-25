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

package me.fromgate.reactions.actions;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.externals.RaEffects;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import me.fromgate.reactions.util.location.Locator;
import me.fromgate.reactions.util.location.Teleporter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ActionTp extends Action {

	@Override
	public boolean execute(Player p, Param params) {
		Location loc = teleportPlayer(p, params);
		if (loc != null) this.setMessageParam(Locator.locationToStringFormatted(loc));
		return (loc != null);
	}

	private Location teleportPlayer(Player p, Param params) {
		Location loc;
		int radius = 0;
		if (params.isEmpty()) return null;
		if (params.isParamsExists("param")) {
			loc = Locator.parseLocation(params.getParam("param", ""), p.getLocation());
		} else {
			loc = Locator.parseLocation(params.getParam("loc", ""), p.getLocation());
			radius = params.getParam("radius", 0);
		}
		boolean land = params.getParam("land", true);

		if (loc != null) {
			if (radius > 0) loc = Locator.getRadiusLocation(loc, radius, land);
			if (Cfg.centerTpCoords) {
				loc.setX(loc.getBlockX() + 0.5);
				loc.setZ(loc.getBlockZ() + 0.5);
			}
			try {
				while (!loc.getChunk().isLoaded()) loc.getChunk().load();
			} catch (Exception ignore) {}

			Variables.setTempVar("loc-from", Locator.locationToString(p.getLocation()));
			Variables.setTempVar("loc-from-str", Locator.locationToStringFormatted(p.getLocation()));
			Variables.setTempVar("loc-to", Locator.locationToString(loc));
			Variables.setTempVar("loc-to-str", Locator.locationToStringFormatted(loc));
			Teleporter.teleport(p, loc);
			String playeffect = params.getParam("effect", "");
			if (!playeffect.isEmpty()) {
				if (playeffect.equalsIgnoreCase("smoke") && (!params.isParamsExists("wind"))) params.set("wind", "all");
				RaEffects.playEffect(loc, playeffect, params);
			}
		}
		return loc;
	}

}
