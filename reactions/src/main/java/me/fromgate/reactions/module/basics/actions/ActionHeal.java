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

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.mob.EntityUtils;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("HP")
public class ActionHeal implements Action {

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        if (player == null) return false;
        double hp = params.getInteger("hp");
        if (params.contains("params")) hp = params.getInteger("params");
        double health = player.getHealth();
        double healthMax = EntityUtils.getMaxHealth(player);
        if (health < healthMax && hp >= 0) {
            player.setHealth(hp == 0 ? healthMax : Math.min(hp + health, healthMax));
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "HEAL";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
