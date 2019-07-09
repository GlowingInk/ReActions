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
import me.fromgate.reactions.event.RespawnEvent;
import me.fromgate.reactions.util.Variables;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;

public class PlayerRespawnActivator extends Activator {

	private PlayerDeathActivator.DeathCause deathCause;

	public PlayerRespawnActivator(String name, String param) {
		super(name, "activators");
		PlayerDeathActivator.DeathCause ds = PlayerDeathActivator.DeathCause.byName(param);
		this.deathCause = ds == null ? PlayerDeathActivator.DeathCause.PVP : ds;
	}

	public PlayerRespawnActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
	}

	@Override
	public boolean activate(Event event) {
		if (!(event instanceof RespawnEvent)) return false;
		RespawnEvent pe = (RespawnEvent) event;
		if (this.deathCause != PlayerDeathActivator.DeathCause.ANY && pe.getDeathCause() != this.deathCause)
			return false;
		Variables.setTempVar("cause", pe.getDeathCause().name());
		if (pe.getKiller() != null) {
			Variables.setTempVar("killer-type", pe.getKiller().getType().name());
			if (pe.getKiller().getType() == EntityType.PLAYER) {
				Variables.setTempVar("targetplayer", pe.getKiller().getName());
				Variables.setTempVar("killer-name", pe.getKiller().getName());
			} else {
				String mobName = pe.getKiller().getCustomName();
				Variables.setTempVar("killer-name", mobName == null || mobName.isEmpty() ? pe.getKiller().getType().name() : mobName);
			}
		}
		return Actions.executeActivator(pe.getPlayer(), this);
	}

	@Override
	public boolean isLocatedAt(Location loc) {
		return false;
	}

	@Override
	public void save(String root, YamlConfiguration cfg) {
		cfg.set(root + ".death-cause", this.deathCause != null ? this.deathCause.name() : "PVP");
	}

	@Override
	public void load(String root, YamlConfiguration cfg) {
		String deathStr = cfg.getString(root + ".death-cause", "PVP");
		this.deathCause = PlayerDeathActivator.DeathCause.byName(deathStr);
		if (this.deathCause == null) this.deathCause = PlayerDeathActivator.DeathCause.PVP;
		//this.item=cfg.getString(root+".item");
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.PLAYER_RESPAWN;
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
		sb.append("(").append(this.deathCause.name()).append(")");
		return sb.toString();
	}
}