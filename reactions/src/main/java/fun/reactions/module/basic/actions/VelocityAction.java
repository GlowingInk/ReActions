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

package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("VECTOR")
public class VelocityAction implements Action, Activity.Personal { // TODO Player selector
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String velstr;
        boolean kick = false;
        if (params.contains("param")) {
            velstr = params.getString("param");
        } else {
            velstr = params.getString("vector");
            if (velstr.isEmpty()) velstr = params.getString("direction");
            kick = params.getBoolean("kick", false);
        }

        if (velstr.isEmpty()) return false;
        Vector velocity = player.getVelocity();
        String[] ln = velstr.split(",");
        if ((ln.length == 1) && (NumberUtils.IS_NUMBER.test(velstr))) {
            double power = Double.parseDouble(velstr);
            velocity.setY(Math.min(10, kick ? power * player.getVelocity().getY() : power));
        } else if ((ln.length == 3) &&
                NumberUtils.IS_NUMBER.test(ln[0]) &&
                NumberUtils.IS_NUMBER.test(ln[1]) &&
                NumberUtils.IS_NUMBER.test(ln[2])) {
            double powerx = Double.parseDouble(ln[0]);
            double powery = Double.parseDouble(ln[1]);
            double powerz = Double.parseDouble(ln[2]);
            if (kick) {
                velocity = player.getLocation().getDirection();
                velocity = velocity.normalize();
                velocity = velocity.multiply(new Vector(powerx, powery, powerz));
                player.setFallDistance(0);
            } else velocity = new Vector(Math.min(10, powerx), Math.min(10, powery), Math.min(10, powerz));
        }
        player.setVelocity(velocity);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "VELOCITY";
    }
}
