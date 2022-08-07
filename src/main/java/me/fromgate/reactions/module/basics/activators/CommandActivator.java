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


package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.CommandStorage;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Aliases("CMD")
public class CommandActivator extends Activator {
    // Full command
    private final String command;
    // Check just command?
    private final boolean checkExact;
    // Check it by start?
    private final boolean starts;
    // List of arguments, if not checkExact
    private final List<String> args;
    // Use regex?
    private final boolean useRegex;
    // Pattern if useRegex
    private final Pattern pattern;
    // Is console allowed to perform this command?
    private final boolean consoleAllowed;

    private CommandActivator(ActivatorLogic base, String command, boolean starts, boolean useRegex, boolean consoleAllowed) {
        super(base);
        command = command == null ? "unknown" : command;
        this.command = command;
        Parameters cmdParams = Parameters.fromString(command);
        if (cmdParams.contains("cmd")) {
            this.args = new ArrayList<>();
            this.args.add(cmdParams.getString("cmd"));
            int i = 1;
            while (cmdParams.contains("arg" + i))
                this.args.add(cmdParams.getString("arg" + i++));
            this.checkExact = false;
            this.pattern = null;
        } else {
            this.args = null;
            this.checkExact = true;
            this.pattern = useRegex ? Pattern.compile(command) : null;
        }
        this.starts = starts;
        this.useRegex = useRegex;
        this.consoleAllowed = consoleAllowed;
    }

    public static CommandActivator create(ActivatorLogic base, Parameters param) {
        String command = param.getString("command", param.toString());
        boolean starts = param.getBoolean("starts", true);
        boolean useRegex = param.getBoolean("regex", false);
        boolean consoleAllowed = param.getBoolean("console", true);
        return new CommandActivator(base, command, starts, useRegex, consoleAllowed);
    }

    public static CommandActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        String command = cfg.getString("command");
        boolean starts = cfg.getBoolean("starts", true);
        boolean useRegex = cfg.getBoolean("regex", false);
        boolean consoleAllowed = cfg.getBoolean("console_allowed", true);
        return new CommandActivator(base, command, starts, useRegex, consoleAllowed);
    }

    @Override
    public boolean checkStorage(Storage storage) {
        CommandStorage cs = (CommandStorage) storage;
        if (!consoleAllowed && cs.getSender() instanceof ConsoleCommandSender) return false;
        if (checkExact) {
            if (useRegex) {
                return pattern.matcher(cs.getCommand()).matches();
            } else
                return starts ?
                        cs.getCommand().toLowerCase(Locale.ENGLISH).startsWith(command) :
                        command.equalsIgnoreCase(cs.getCommand());
        } else {
            if (args.size() != cs.getArgs().length + 1) return false;
            if (!args.get(0).equalsIgnoreCase(cs.getLabel())) return false;
            for (int i = 1; i <= cs.getArgs().length; i++) {
                String arg = args.get(i);
                if (arg.equals("*")) continue;
                if (!arg.equalsIgnoreCase(cs.getArgs()[i - 1])) return false;
            }
            return true;
        }
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("regex", useRegex);
        cfg.set("command", command);
        cfg.set("console_allowed", consoleAllowed);
    }

    @Override
    public boolean isValid() {
        return !Utils.isStringEmpty(command);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "regex:" + this.useRegex +
                "; command:" + this.command +
                "; console:" + this.consoleAllowed +
                ")";
        return sb;
    }
}
