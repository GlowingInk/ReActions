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

import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("GRPADD")
public class GroupAddAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        return RaVault.playerAddGroup(context.getPlayer(), params);
    }

    @Override
    public @NotNull String getName() {
        return "GROUP_ADD";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}