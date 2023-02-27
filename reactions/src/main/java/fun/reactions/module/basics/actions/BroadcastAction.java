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

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.basics.ReActionsModule;
import fun.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("MSGALL")
public class BroadcastAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Bukkit.broadcast(ReActionsModule.getMineDown(content).toComponent());
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "BROADCAST";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}