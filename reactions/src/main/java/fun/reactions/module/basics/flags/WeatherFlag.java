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

package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WeatherFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        Player player = env.getPlayer();
        if (params.equalsIgnoreCase("rain")) return !player.getWorld().isThundering() && player.getWorld().hasStorm();
        if (params.equalsIgnoreCase("thunder")) return player.getWorld().isThundering() && player.getWorld().hasStorm();
        return !player.getWorld().hasStorm();
    }

    @Override
    public @NotNull String getName() {
        return "WEATHER";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
