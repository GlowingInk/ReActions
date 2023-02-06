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

package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.CommandActivator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.*;

public class CommandDetails extends Details {

    private final String label, command;
    private final List<String> args;
    private final CommandSender sender;

    public CommandDetails(Player p, CommandSender sender, String command) {
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
        vars.put(CANCEL_EVENT, property(false));
        String[] start = label.split(":", 2);
        if (start.length == 1) {
            vars.put("prefix", simple(start[0]));
            vars.put("label", simple(start[0]));
        } else {
            vars.put("prefix", simple(start[0]));
            vars.put("label", simple(start[1]));
        }
        vars.put("args", lazy(() -> String.join(" ", args)));
        vars.put("args0", lazy(() -> String.join(" ", args)));
        vars.put("command", simple(command));
        vars.put("argscount", simple(args.size()));
        vars.put("arg0", simple(label));
        for (int i = 0; i < args.size(); i++) {
            int j = i + 1;
            vars.put("arg" + j, simple(args.get(i)));
            int index = i;
            vars.put("args" + j, lazy(() -> String.join(" ", args.subList(index, args.size()))));
        }
        return vars;
    }

    public String getLabel() {return this.label;}

    public String getCommand() {return this.command;}

    public List<String> getArgs() {return this.args;}

    public CommandSender getSender() {return this.sender;}
}
