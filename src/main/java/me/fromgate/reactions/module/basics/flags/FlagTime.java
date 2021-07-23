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

import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FlagTime implements OldFlag {

    @Override
    public boolean checkFlag(RaContext context, String time) {
        Player player = context.getPlayer();
        saveTempVar(context, time);
        long currentTime = Bukkit.getWorlds().get(0).getTime();
        if (player != null) currentTime = player.getWorld().getTime();

        if (time.equalsIgnoreCase("day")) {
            return ((currentTime >= 0) && (currentTime < 12000));
        } else if (time.equalsIgnoreCase("night")) {
            return ((currentTime >= 12000) && (currentTime < 23999));

        } else {
            String[] tln = time.split(",");
            if (tln.length > 0) {
                for (String timeStr : tln)
                    if (NumberUtils.INT_POSITIVE.matcher(timeStr).matches()) {
                        int ct = (int) ((currentTime / 1000 + 6) % 24);
                        if (ct == Integer.parseInt(timeStr)) return true;
                    }
            }
        }
        return false;
    }

    private void saveTempVar(RaContext context, String time) {
        StringBuilder result = new StringBuilder(time);
        if (!(time.equals("day") || time.equals("night"))) {
            String[] ln = time.split(",");
            if (ln.length > 0)
                for (int i = 0; i < ln.length; i++) {
                    if (!NumberUtils.isInteger(ln[i])) continue;
                    String tmp = String.format("%02d:00", Integer.parseInt(ln[i]));
                    if (i == 0) result = new StringBuilder(tmp);
                    else result.append(", ").append(tmp);
                }
        }
        context.setVariable("TIME", result.toString());
    }
}

