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
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.data.RaContext;
import org.bukkit.entity.Player;


/**
 * Если два указанных игрока находятся в определенном отношении
 * Параметры: <игрок1> <игрок2> <отношение>
 * Список отношений: LEADER, OFFICER, MEMBER, RECRUIT, ALLY, TRUCE, NEUTRAL, ENEMY
 */

public class FlagPlayersInRel implements Flag {
	@Override
	public boolean checkFlag(RaContext context, String param) {
		Player player = context.getPlayer();
		if (!Externals.isConnectedFactions()) return false;
		String[] params = param.split("\\s");
		Player player1 = Util.getPlayerExact(params[0].trim());
		Player player2 = Util.getPlayerExact(params[1].trim());
		String targetRel = params[2].trim();
		String playersRel = RaFactions.getRelationWith(player1, RaFactions.getPlayerFaction(player2));
		return targetRel.equalsIgnoreCase(playersRel);
	}
}
