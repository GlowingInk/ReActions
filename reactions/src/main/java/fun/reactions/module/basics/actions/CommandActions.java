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

package fun.reactions.module.basics.actions;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.TemporaryOp;
import fun.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class CommandActions implements Action, Aliased {

    private final Type commandAs;

    public CommandActions(Type commandAs) {
        this.commandAs = commandAs;
    }

    private static void dispatchCommand(final boolean setOp, final CommandSender sender, final String commandLine) {
        Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> {
            if (setOp) {
                if (Cfg.altOperator) {
                    Bukkit.dispatchCommand(TemporaryOp.asOp(sender), commandLine);
                } else {
                    TemporaryOp.setOp(sender);
                    Bukkit.dispatchCommand(sender, commandLine);
                    TemporaryOp.removeOp(sender);
                }
            } else Bukkit.dispatchCommand(sender, commandLine);
        });
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Player player = env.getPlayer();
        if (commandAs != Type.CONSOLE && player == null) return false;
        switch (commandAs) {
            default -> dispatchCommand(false, player, paramsStr);
            case OP -> dispatchCommand(true, player, paramsStr);
            case CONSOLE -> dispatchCommand(false, Bukkit.getConsoleSender(), paramsStr);
            case CHAT -> player.chat("/" + paramsStr);
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return switch (commandAs) {
            case NORMAL -> "COMMAND";
            case OP -> "COMMAND_OP";
            case CONSOLE -> "COMMAND_CONSOLE";
            case CHAT -> "COMMAND_CHAT";
        };
    }

    @Override
    public @NotNull Collection<@NotNull String> getAliases() {
        return List.of(switch (commandAs) {
            case NORMAL -> "CMD";
            case OP -> "CMD_OP";
            case CONSOLE -> "CMD_CONSOLE";
            case CHAT -> "CMD_CHAT";
        });
    }

    public enum Type {
        NORMAL, OP, CONSOLE, CHAT
    }
}
