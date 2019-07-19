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
import me.fromgate.reactions.event.CommandEvent;
import me.fromgate.reactions.event.RAEvent;
import me.fromgate.reactions.util.FakeCmd;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.Variables;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashSet;
import java.util.Set;

public class CommandActivator extends Activator {

	private boolean checkExact;
	private String command;
	private Param arguments = new Param();
	private boolean useRegex;
	boolean override;

	public void init() {
		if (command == null) return;
		Param params = new Param(command);
		if (params.isParamsExists("cmd")) {
			checkExact = true;
			arguments = params;
		}
		params.remove("param-line");
	}

	CommandActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
		init();
	}

	public CommandActivator(String name, String param) {
		super(name, "activators");
		Param cmdParam = new Param(param);
		if (cmdParam.isParamsExists("command")) {
			command = cmdParam.getParam("command");
			override = cmdParam.getParam("override", true);
			useRegex = cmdParam.getParam("regex", false);
		} else {
			command = param;
			override = true;
			useRegex = false;
		}
		init();
	}

	private boolean checkLine(String line) {
		if (this.useRegex) return line.matches(command);
		return line.toLowerCase().startsWith(command.toLowerCase());
	}

	public boolean commandMatches(String line) {
		if (!this.checkExact) return checkLine(line);
		String[] cmdLn = line.replaceFirst("/", "").split(" ");
		if (cmdLn.length == 0) return false;
		Set<String> keys = new HashSet<>();
		for (int i = 0; i < cmdLn.length; i++) {
			String key = (i == 0 ? "cmd" : "arg" + i);
			keys.add(key);
			if (!arguments.hasAnyParam(key)) return false;
			if (arguments.getParam(key).equalsIgnoreCase("*")) continue;
			if (!arguments.getParam(key).equalsIgnoreCase(cmdLn[i])) return false;
		}
		return arguments.keySet().equals(keys);
	}

	private void setTempVars(String command, String[] args) {
		String argStr = args.length > 1 ? command.replaceAll("^" + args[0] + " ", "") : "";
		Variables.setTempVar("command", command);
		Variables.setTempVar("args", argStr);
		String argsLeft = command.replaceAll("(\\S+ )+{" + args.length + "}", "");
		Variables.setTempVar("argsleft", argsLeft);
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++)
				Variables.setTempVar("arg" + i, args[i]);
		}

		int count = 0;
		while (argStr.indexOf(" ") > 0) {
			count++;
			argStr = argStr.substring(argStr.indexOf(" ") + 1);
			Variables.setTempVar("args" + count, argStr);
			Variables.setTempVar("argscount", Integer.toString(count + 1));
		}
	}

	@Override
	public boolean activate(RAEvent event) {
		if (!(event instanceof CommandEvent)) return false;
		CommandEvent ce = (CommandEvent) event;
		if (ce.isParentCancelled() && !this.override) return false;
		if (!commandMatches(ce.getCommand())) return false;
		setTempVars(ce.getCommand(), ce.getArgs());
		if (!isCommandRegistered(ce.getCommand()) && FakeCmd.registerNewCommand(ce.getCommand())) {
			Msg.CMD_REGISTERED.log(ce.getCommand());
		}
		return Actions.executeActivator(ce.getPlayer(), this);
	}

	public String getCommand() {
		if (this.checkExact) {
			return arguments.getParam("cmd", "");
		} else {
			String[] cmd = this.command.split(" ");
			if (cmd.length == 0) return "";
			return cmd[0];
		}
	}

	public boolean isCommandRegistered() {
		String command = getCommand();
		if (command.isEmpty()) return false;
		return isCommandRegistered(command);
	}


	public boolean isCommandRegistered(String cmd) {
		if (cmd.isEmpty()) return false;
		Command cmm = Bukkit.getServer().getPluginCommand(cmd);
		return (cmm != null);
	}

	@Override
	public void save(ConfigurationSection cfg) {
		cfg.set("override", this.override);
		cfg.set("regex", this.useRegex);
		cfg.set("command", command);
	}

	@Override
	public void load(ConfigurationSection cfg) {
		this.override = cfg.getBoolean("override", true);
		this.useRegex = cfg.getBoolean("regex", false);
		this.command = cfg.getString("command");
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.COMMAND;
	}

	@Override
	public boolean isValid() {
		return !Util.emptySting(command);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
		if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
		if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
		if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
		sb.append(" (override:").append(this.override);
		if (this.useRegex) sb.append(" regex:true");
		sb.append(" command:").append(this.command).append(")");
		return sb.toString();
	}

	public boolean useRegex() {
		return this.useRegex;
	}

}
