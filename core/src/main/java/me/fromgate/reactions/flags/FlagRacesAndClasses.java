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

package me.fromgate.reactions.flags;

import me.fromgate.reactions.externals.RaRacesAndClasses;
import org.bukkit.entity.Player;

public class FlagRacesAndClasses implements Flag {

	private final boolean checkRace;

	public FlagRacesAndClasses(boolean checkRace) {
		this.checkRace = checkRace;
	}

	@Override
	public boolean checkFlag(Player player, String param) {
		if (!RaRacesAndClasses.isEnabled()) return false;
		return this.checkRace ? RaRacesAndClasses.checkRace(player, param) : RaRacesAndClasses.checkClass(player, param);
	}

}
