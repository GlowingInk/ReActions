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

import me.fromgate.reactions.externals.RaEffects;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.mob.EntityUtil;
import org.bukkit.entity.Player;

public class ActionHeal extends Action {

	@Override
	public boolean execute(Player p, Param params) {
		Player player;
		double hp = params.getParam("hp", 0);
		boolean playhearts = params.getParam("hearts", true);
		if (params.isParamsExists("params")) hp = params.getParam("params", 0);
		String playerName = params.getParam("player", p != null ? p.getName() : "");
		player = playerName.isEmpty() ? null : Util.getPlayerExact(playerName);
		if (player == null) return false;
		double health = player.getHealth();
		double healthMax = EntityUtil.getMaxHealth(player);
		if (health < healthMax && hp >= 0) {
			player.setHealth(hp == 0 ? healthMax : Math.min(hp + health, healthMax));
		}
		if (playhearts && RaEffects.isPlayEffectConnected()) {
			RaEffects.playEffect(player.getEyeLocation(), "HEART", "offset:0.5 num:4 speed:0.7");
		}
		setMessageParam(Double.toString(hp));
		return true;
	}
}
