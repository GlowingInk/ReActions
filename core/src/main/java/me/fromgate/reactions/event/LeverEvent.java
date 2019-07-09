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

package me.fromgate.reactions.event;

import me.fromgate.reactions.activators.ActivatorType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;

public class LeverEvent extends RAEvent {
	private Block leverBlock;


	public LeverEvent(Player p, Block block) {
		super(p, ActivatorType.LEVER);
		this.leverBlock = block;
	}

	public Lever getLever() {
		return (Lever) leverBlock.getState().getData();
	}

	public boolean isLeverPowered() {
		Lever lever = getLever();
		if (lever == null) return false;
		return lever.isPowered();
	}

	public Location getLeverLocation() {
		return leverBlock.getLocation();
	}

}
