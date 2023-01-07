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
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.NumberUtils.Is;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlagTime implements Flag {

    @Override
    public @NotNull String getName() {
        return "TIME";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        saveTempVar(context, params);
        long currentTime = Bukkit.getWorlds().get(0).getTime();
        if (player != null) currentTime = player.getWorld().getTime();

        if (params.equalsIgnoreCase("day")) {
            return ((currentTime >= 0) && (currentTime < 12000));
        } else if (params.equalsIgnoreCase("night")) {
            return ((currentTime >= 12000) && (currentTime < 23999));

        } else {
            String[] tln = params.split(",");
            if (tln.length > 0) {
                for (String timeStr : tln)
                    if (NumberUtils.isNumber(timeStr, Is.NATURAL)) {
                        int ct = (int) ((currentTime / 1000 + 6) % 24);
                        if (ct == Integer.parseInt(timeStr)) return true;
                    }
            }
        }
        return false;
    }

    private void saveTempVar(RaContext context, String time) {
        StringBuilder result = new StringBuilder(time);
        if (!(time.equalsIgnoreCase("day") || time.equalsIgnoreCase("night"))) {
            String[] ln = time.split(",");
            if (ln.length > 0)
                for (int i = 0; i < ln.length; i++) {
                    if (!NumberUtils.isNumber(ln[i], Is.NATURAL)) continue;
                    String tmp = String.format("%02d:00", Integer.parseInt(ln[i]));
                    if (i == 0) result = new StringBuilder(tmp);
                    else result.append(", ").append(tmp);
                }
        }
        context.setVariable("time", result.toString());
    }
}

