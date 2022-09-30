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

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.alias.Aliases;
import me.fromgate.reactions.util.math.MathUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Aliases("VECTOR")
public class ActionVelocity implements Action {

    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Vector v = setPlayerVelocity(context.getPlayer(), params);
        return v != null;
    }

    private Vector setPlayerVelocity(Player p, Parameters params) {
        String velstr;
        boolean kick = false;
        if (params.contains("param")) {
            velstr = params.getString("param");
        } else {
            velstr = params.getString("vector");
            if (velstr.isEmpty()) velstr = params.getString("direction");
            kick = params.getBoolean("kick", false);
        }

        if (velstr.isEmpty()) return null;
        Vector v = p.getVelocity();
        String[] ln = velstr.split(",");
        if ((ln.length == 1) && (MathUtils.FLOAT.matcher(velstr).matches())) {
            double power = Double.parseDouble(velstr);
            v.setY(Math.min(10, kick ? power * p.getVelocity().getY() : power));
        } else if ((ln.length == 3) &&
                MathUtils.FLOAT.matcher(ln[0]).matches() &&
                MathUtils.FLOAT.matcher(ln[1]).matches() &&
                MathUtils.FLOAT.matcher(ln[2]).matches()) {
            double powerx = Double.parseDouble(ln[0]);
            double powery = Double.parseDouble(ln[1]);
            double powerz = Double.parseDouble(ln[2]);
            if (kick) {
                v = p.getLocation().getDirection();
                v = v.normalize();
                v = v.multiply(new Vector(powerx, powery, powerz));
                p.setFallDistance(0);
            } else v = new Vector(Math.min(10, powerx), Math.min(10, powery), Math.min(10, powerz));
        }
        p.setVelocity(v);
        return v;
    }

    @Override
    public @NotNull String getName() {
        return "VELOCITY";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
