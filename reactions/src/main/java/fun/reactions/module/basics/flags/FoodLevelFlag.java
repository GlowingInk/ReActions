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
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"FOODLEVEL", "FOOD"})
public class FoodLevelFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Player player = env.getPlayer();
        if (!NumberUtils.isNumber(paramsStr, Is.NATURAL)) return false;
        return player.getFoodLevel() >= Integer.parseInt(paramsStr);
    }

    @Override
    public @NotNull String getName() {
        return "FOOD_LEVEL";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
