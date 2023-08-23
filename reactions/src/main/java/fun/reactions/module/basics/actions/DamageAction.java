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
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("DMG")
public class DamageAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        double damage = params.getInteger("damage", () -> params.getInteger(Parameters.ORIGIN));
        if (params.contains("player")) {
            // TODO: Selector?
            player = Bukkit.getPlayerExact(params.getString("player"));
        }
        return damagePlayer(player, damage);
    }

    @Override
    public @NotNull String getName() {
        return "DAMAGE";
    }

    private boolean damagePlayer(Player player, double damage) {
        if (player == null || player.isDead() || !player.isOnline()) return false;
        if (damage > 0) {
            player.damage(damage);
        } else {
            player.playEffect(EntityEffect.HURT);
        }
        return true;
    }
}
