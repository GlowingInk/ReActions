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

package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.alias.Aliases;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

@Aliases({"GM", "GAME_MODE"})
public class FlagGamemode implements Flag {
    @Override
    public boolean check(@NotNull RaContext context, @NotNull String params) {
        return context.getPlayer().getGameMode() == Utils.getEnum(GameMode.class, params);
    }

    @Override
    public @NotNull String getName() {
        return "GAMEMODE";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
