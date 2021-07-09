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

package me.fromgate.reactions.module.defaults.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.mob.EntityUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Alias("HP")
public class ActionHeal extends Action {

    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        if (params.contains("player"))
            player = Utils.getPlayerExact(params.getString("player"));
        if (player == null) return false;
        double hp = params.getInteger("hp", 0);
        if (params.contains("params")) hp = params.getInteger("params", 0);
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
