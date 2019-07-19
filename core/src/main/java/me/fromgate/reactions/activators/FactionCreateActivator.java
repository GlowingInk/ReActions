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
import me.fromgate.reactions.storage.FactionCreateStorage;
import me.fromgate.reactions.storage.RAStorage;
import me.fromgate.reactions.util.Variables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class FactionCreateActivator extends Activator {

	public FactionCreateActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
	}

	public FactionCreateActivator(String name, String param) {
		super(name, "activators");
	}

	@Override
	public boolean activate(RAStorage event) {
		FactionCreateStorage fe = (FactionCreateStorage) event;
		Variables.setTempVar("faction", fe.getFaction());
		return Actions.executeActivator(fe.getPlayer(), this);
	}

	@Override
	public void save(ConfigurationSection cfg) {
	}

	@Override
	public void load(ConfigurationSection cfg) {
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.FCT_CREATE;
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
