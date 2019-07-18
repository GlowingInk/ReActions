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
import me.fromgate.reactions.event.JoinEvent;
import me.fromgate.reactions.event.RAEvent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class JoinActivator extends Activator {

	private boolean firstJoin;

	JoinActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
	}

	public JoinActivator(String name, String join) {
		super(name, "activators");
		this.firstJoin = join.equalsIgnoreCase("first") || join.equalsIgnoreCase("firstjoin");
	}

	@Override
	public boolean activate(RAEvent event) {
		if (event instanceof JoinEvent) {
			JoinEvent ce = (JoinEvent) event;
			if (isJoinActivate(ce.isFirstJoin())) return Actions.executeActivator(ce.getPlayer(), this);
		}
		return false;
	}

	private boolean isJoinActivate(boolean join_first_time) {
		if (this.firstJoin) return join_first_time;
		return true;
	}

	@Override
	public boolean isLocatedAt(Location loc) {
		return false;
	}

	@Override
	public void save(ConfigurationSection cfg) {
		cfg.set("join-state", (firstJoin ? "FIRST" : "ANY"));
	}

	@Override
	public void load(ConfigurationSection cfg) {
		this.firstJoin = cfg.getString("join-state", "ANY").equalsIgnoreCase("first");
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.JOIN;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
		if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
		if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
		if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
		sb.append(" (first join:").append(this.firstJoin).append(")");
		return sb.toString();
	}

}
