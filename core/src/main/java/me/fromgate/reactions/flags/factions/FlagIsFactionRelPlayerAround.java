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

package me.fromgate.reactions.flags.factions;

import me.fromgate.reactions.externals.Externals;
import me.fromgate.reactions.externals.factions.RaFactions;
import me.fromgate.reactions.flags.Flag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Если вокруг игрока в радиусе находится другой игрок с заданным отношением их фракций
 * Параметры: <радиус> <отношение>
 * Список отношений: LEADER, OFFICER, MEMBER, RECRUIT, ALLY, TRUCE, NEUTRAL, ENEMY
 */
public class FlagIsFactionRelPlayerAround implements Flag {

	@Override
	public boolean checkFlag(Player player, String param) {
		if (!Externals.isConnectedFactions()) return false;
		String[] params = param.split("\\s");
		double radius = Double.valueOf(params[0].trim());
		String targetRel = params[1].trim();

		for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
			if (!(entity instanceof Player)) continue;
			String curTargetFaction = RaFactions.getPlayerFaction((Player) entity);

			if (RaFactions.getRelationWith(player, curTargetFaction).equalsIgnoreCase(targetRel)) return true;
		}
		return false;
	}
}
