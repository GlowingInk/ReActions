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

package me.fromgate.reactions.activators;

import me.fromgate.reactions.storages.LeverStorage;
import me.fromgate.reactions.storages.Storage;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.parameter.BlockParam;
import me.fromgate.reactions.util.parameter.Param;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class LeverActivator extends Activator implements Locatable {

	private final String state; //on, off
	private final String world;
	private final int x;
	private final int y;
	private final int z;

	private LeverActivator(ActivatorBase base, String state, String world, int x, int y, int z) {
		super(base);
		this.state = state;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean activate(Storage event) {
		LeverStorage le = (LeverStorage) event;
		if (le.getLever() == null) return false;
		if (!isLocatedAt(le.getLeverLocation())) return false;
		if (this.state.equalsIgnoreCase("on") && le.isLeverPowered()) return false;
		if (this.state.equalsIgnoreCase("off") && (!le.isLeverPowered())) return false;
		return true;
	}

	@Override
	public boolean isLocatedAt(Location l) {
		if (l == null) return false;
		if (!world.equals(l.getWorld().getName())) return false;
		if (x != l.getBlockX()) return false;
		if (y != l.getBlockY()) return false;
		return (z == l.getBlockZ());
	}

	@Override
	public boolean isLocatedAt(World world, int x, int y, int z) {
		return this.world.equals(world.getName()) &&
				this.x == x &&
				this.y == y &&
				this.z == z;
	}

	@Override
	public void save(ConfigurationSection cfg) {
		cfg.set("world", this.world);
		cfg.set("x", x);
		cfg.set("y", y);
		cfg.set("z", z);
		cfg.set("lever-state", state);
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.LEVER;
	}

	@Override
	public boolean isValid() {
		return !Util.isStringEmpty(world);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" (").append(world).append(", ").append(x).append(", ").append(y).append(", ").append(z);
		sb.append(" state:").append(this.state.toUpperCase()).append(")");
		return sb.toString();
	}

	public static LeverActivator create(ActivatorBase base, Param p) {
		if(!(p instanceof BlockParam)) return null;
		BlockParam param = (BlockParam) p;
		Block targetBlock = param.getBlock();
		String line = param.toString();
		if (targetBlock != null && targetBlock.getType() == Material.LEVER) {
			String state = "ANY";
			if (line.equalsIgnoreCase("on")) state = "ON";
			if (line.equalsIgnoreCase("off")) state = "OFF";
			String world = targetBlock.getWorld().getName();
			int x = targetBlock.getX();
			int y = targetBlock.getY();
			int z = targetBlock.getZ();
			return new LeverActivator(base, state, world, x, y, z);
		} else return null;
	}

	public static LeverActivator load(ActivatorBase base, ConfigurationSection cfg) {
		String world = cfg.getString("world");
		int x = cfg.getInt("x");
		int y = cfg.getInt("y");
		int z = cfg.getInt("z");
		String state = cfg.getString("lever-state", "ANY");
		if ((!state.equalsIgnoreCase("on")) && (!state.equalsIgnoreCase("off"))) state = "ANY";
		return new LeverActivator(base, state, world, x, y, z);
	}
}