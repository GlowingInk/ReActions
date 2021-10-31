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
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Alias("GRPRMV")
public class ActionGroupRemove implements Action {

    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        if (RaVault.playerInGroup(player, params))
            return RaVault.playerRemoveGroup(player, params);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "GROUP_REMOVE";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
