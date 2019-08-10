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

import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.storage.ButtonStorage;
import me.fromgate.reactions.storage.RAStorage;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class ButtonActivator extends Activator implements Locatable {
	private final String world;
	private final int x;
	private final int y;
	private final int z;

	public ButtonActivator(ActivatorBase base, String world, int x, int y, int z) {
		super(base);
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean activate(RAStorage event) {
		ButtonStorage be = (ButtonStorage) event;
		if (!isLocatedAt(be.getButtonLocation())) return false;
		return Actions.executeActivator(be.getPlayer(), getBase());
	}

	@Override
	public boolean isLocatedAt(Location l) {
		if (l == null) return false;
		if (!world.equalsIgnoreCase(l.getWorld().getName())) return false;
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
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.BUTTON;
	}

	@Override
	public boolean isValid() {
		return !Util.emptyString(world);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" (");
		sb.append(world).append(", ").append(x).append(", ").append(y).append(", ").append(z);
		sb.append(")");
		return sb.toString();
	}

	public static ButtonActivator create(ActivatorBase base, Param param) {
		int x = param.getParam("x", 0);
		int y = param.getParam("y", 0);
		int z = param.getParam("z", 0);
		String world = param.getParam("world", Bukkit.getWorlds().get(0).getName());
		return new ButtonActivator(base, world, x, y, z);
	}

	public static ButtonActivator load(ActivatorBase base, ConfigurationSection cfg) {
		int x = cfg.getInt("x", 0);
		int y = cfg.getInt("y", 0);
		int z = cfg.getInt("z", 0);
		String world = cfg.getString("world", Bukkit.getWorlds().get(0).getName());
		return new ButtonActivator(base, world, x, y, z);
	}

}
