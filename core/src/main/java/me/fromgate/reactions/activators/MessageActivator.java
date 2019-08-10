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
import me.fromgate.reactions.storage.MessageStorage;
import me.fromgate.reactions.storage.RAStorage;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.Variables;
import org.bukkit.configuration.ConfigurationSection;

import java.util.regex.Pattern;

public class MessageActivator extends Activator {
	private final static Pattern NOT_D = Pattern.compile("\\D+");

	private CheckType type;
	private Source source;
	private String mask;

	public MessageActivator(ActivatorBase base, CheckType type, Source source, String mask) {
		super(base);
		this.type = type;
		this.source = source;
		this.mask = mask;
	}

	@Override
	public void save(ConfigurationSection cfg) {
		cfg.set("mask", mask);
		cfg.set("type", type.name());
		cfg.set("source", source.name());
	}

	public enum CheckType {
		REGEX,
		CONTAINS,
		EQUAL,
		START,
		END;

		public static CheckType getByName(String name) {
			if (name.equalsIgnoreCase("contain")) return CheckType.CONTAINS;
			if (name.equalsIgnoreCase("equals")) return CheckType.EQUAL;
			for (CheckType t : CheckType.values()) {
				if (t.name().equalsIgnoreCase(name)) return t;
			}
			return CheckType.EQUAL;
		}

		public static boolean isValid(String name) {
			for (CheckType t : CheckType.values()) {
				if (t.name().equalsIgnoreCase(name)) return true;
			}
			return false;
		}
	}


	public enum Source {
		ALL,
		CHAT_INPUT,
		CONSOLE_INPUT,
		CHAT_OUTPUT,
		LOG_OUTPUT;
		//ANSWER;

		public static Source getByName(String name) {
			for (Source source : Source.values()) {
				if (source.name().equalsIgnoreCase(name)) return source;
			}
			return Source.ALL;
		}

		public static boolean isValid(String name) {
			for (Source source : Source.values()) {
				if (source.name().equalsIgnoreCase(name)) return true;
			}
			return false;
		}
	}


	@Override
	public boolean activate(RAStorage event) {
		MessageStorage e = (MessageStorage) event;
		if (!e.isForActivator(this)) return false;
		setTempVars(e.getMessage());
		return Actions.executeActivator(e.getPlayer(), this);
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.MESSAGE;
	}

	@Override
	public boolean isValid() {
		return !Util.emptyString(mask);
	}

	public boolean filterMessage(Source source, String message) {
		if (source != this.source && this.source != Source.ALL) return false;
		return filter(message);
	}

	private boolean filter(String message) {
		switch (type) {
			case CONTAINS:
				return message.toLowerCase().contains(this.mask.toLowerCase());
			case END:
				return message.toLowerCase().endsWith(this.mask.toLowerCase());
			case EQUAL:
				return message.equalsIgnoreCase(this.mask);
			case REGEX:
				return message.matches(this.mask);
			case START:
				return message.toLowerCase().startsWith(this.mask.toLowerCase());
		}
		return false;
	}

	private void setTempVars(String message) {
		Variables.setTempVar("message", message);
		String[] args = message.split(" ");
		int countInt = 0;
		int countNum = 0;
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				Variables.setTempVar("word" + (i + 1), args[i]);
				Variables.setTempVar("wnum" + (i + 1), NOT_D.matcher(args[i]).replaceAll(""));
				if (Util.INT_NEG.matcher(args[i]).matches()) {
					countInt++;
					Variables.setTempVar("int" + countInt, args[i]);
				}
				if (Util.FLOAT_NEG.matcher(args[i]).matches()) {
					countNum++;
					Variables.setTempVar("num" + countNum, args[i]);
				}

			}
		}
		Variables.setTempVar("word-count", Integer.toString(args.length));
		Variables.setTempVar("int-count", Integer.toString(countInt));
		Variables.setTempVar("num-count", Integer.toString(countNum));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
		if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
		if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
		if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
		sb.append(" (");
		sb.append("type:").append(this.type.name());
		sb.append(" source:").append(this.source.name());
		sb.append(" mask:").append(this.mask);
		sb.append(")");
		return sb.toString();
	}

	public static MessageActivator create(ActivatorBase base, Param param) {
		CheckType type = CheckType.getByName(param.getParam("type", "EQUAL"));
		Source source = Source.getByName(param.getParam("source", "CHAT_MESSAGE"));
		String mask = param.getParam("mask", param.getParam("message", "Message mask"));
		return new MessageActivator(base, type, source, mask);
	}

	public static MessageActivator load(ActivatorBase base, ConfigurationSection cfg) {
		CheckType type = CheckType.getByName(cfg.getString("type", "EQUAL"));
		Source source = Source.getByName(cfg.getString("source", "CHAT_INPUT"));
		String mask = cfg.getString("mask", "Message mask");
		return new MessageActivator(base, type, source, mask);
	}
}
