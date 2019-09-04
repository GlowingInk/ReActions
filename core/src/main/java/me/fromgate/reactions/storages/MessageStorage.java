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


package me.fromgate.reactions.storages;

import lombok.Getter;
import me.fromgate.reactions.activators.ActivatorType;
import me.fromgate.reactions.activators.MessageActivator;
import org.bukkit.entity.Player;

public class MessageStorage extends Storage {
	@Getter private final String message;
	private final MessageActivator activator;


	public MessageStorage(Player player, MessageActivator activator, String message) {
		super(player, ActivatorType.MESSAGE);
		this.activator = activator;
		this.message = message;
	}

	public boolean isForActivator(MessageActivator messageActivator) {
		return this.activator.equals(messageActivator);
	}

}
