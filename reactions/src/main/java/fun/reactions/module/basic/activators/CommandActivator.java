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


package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.util.Utils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

@Aliased.Names("CMD")
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

    private CommandActivator(Logic base, String command, boolean starts, boolean useRegex, boolean consoleAllowed) {
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

    public static CommandActivator create(Logic base, Parameters param) {
        String command = param.getString("command", param.originValue());
        boolean starts = param.getBoolean("starts", true);
        boolean useRegex = param.getBoolean("regex", false);
        boolean consoleAllowed = param.getBoolean("console", true);
        return new CommandActivator(base, command, starts, useRegex, consoleAllowed);
    }

    public static CommandActivator load(Logic base, ConfigurationSection cfg) {
        String command = cfg.getString("command");
        boolean starts = cfg.getBoolean("starts", true);
        boolean useRegex = cfg.getBoolean("regex", false);
        boolean consoleAllowed = cfg.getBoolean("console_allowed", true);
        return new CommandActivator(base, command, starts, useRegex, consoleAllowed);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context cs = (Context) context;
        if (!consoleAllowed && cs.sender instanceof ConsoleCommandSender) return false;
        if (checkExact) {
            if (useRegex) {
                return pattern.matcher(cs.command).matches();
            } else
                return starts ?
                        cs.command.toLowerCase(Locale.ROOT).startsWith(command) :
                        command.equalsIgnoreCase(cs.command);
        } else {
            if (args.size() != cs.args.size() + 1) return false;
            if (!args.get(0).equalsIgnoreCase(cs.label)) return false;
            for (int i = 1; i <= cs.args.size(); i++) {
                String arg = args.get(i);
                if (arg.equals("*")) continue;
                if (!arg.equalsIgnoreCase(cs.args.get(i - 1))) return false;
            }
            return true;
        }
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
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

    public static class Context extends ActivationContext {

        private final String label, command;
        private final List<String> args;
        private final CommandSender sender;

        public Context(Player p, CommandSender sender, String command) {
            super(p);
            this.sender = sender;
            this.command = command;
            String[] split = command.split("\\s");
            this.label = split[0];
            this.args = Arrays.asList(Arrays.copyOfRange(split, 1, split.length));
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return CommandActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put(CANCEL_EVENT, Variable.property(false));
            String[] start = label.split(":", 2);
            if (start.length == 1) {
                vars.put("prefix", Variable.simple(start[0]));
                vars.put("label", Variable.simple(start[0]));
            } else {
                vars.put("prefix", Variable.simple(start[0]));
                vars.put("label", Variable.simple(start[1]));
            }
            vars.put("args", Variable.lazy(() -> String.join(" ", args)));
            vars.put("args0", Variable.lazy(() -> String.join(" ", args)));
            vars.put("command", Variable.simple(command));
            vars.put("argscount", Variable.simple(args.size()));
            vars.put("arg0", Variable.simple(label));
            for (int i = 0; i < args.size(); i++) {
                int j = i + 1;
                vars.put("arg" + j, Variable.simple(args.get(i)));
                int index = i;
                vars.put("args" + j, Variable.lazy(() -> String.join(" ", args.subList(index, args.size()))));
            }
            return vars;
        }

        public String getLabel() {
            return label;
        }

        public String getCommand() {
            return command;
        }

        public CommandSender getSender() {
            return sender;
        }

        public List<String> getArgs() {
            return args;
        }
    }
}
