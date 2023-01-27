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

package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.Cfg;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.TemporaryOp;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionCommand implements Action {

    private final Type commandAs;

    public ActionCommand(Type commandAs) {
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
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        Player player = context.getPlayer();
        if (commandAs != Type.CONSOLE && player == null) return false;
        String commandLine = params;
        switch (commandAs) {
            default -> dispatchCommand(false, player, commandLine);
            case OP -> dispatchCommand(true, player, commandLine);
            case CONSOLE -> dispatchCommand(false, Bukkit.getConsoleSender(), commandLine);
            case CHAT -> {
                commandLine = commandLine.replaceFirst("/", "");
                player.chat("/" + commandLine);
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return switch (commandAs) {
            case NORMAL -> "CMD";
            case OP -> "CMD_OP";
            case CONSOLE -> "CMD_CONSOLE";
            case CHAT -> "CMD_CHAT";
        };
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    public enum Type {
        NORMAL, OP, CONSOLE, CHAT
    }
}
