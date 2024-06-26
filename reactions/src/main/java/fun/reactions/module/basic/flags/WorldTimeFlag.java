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

package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

@Aliased.Names("TIME")
public class WorldTimeFlag implements Flag { // TODO Rework, support specific worlds

    @Override
    public @NotNull String getName() {
        return "WORLD_TIME";
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Player player = env.getPlayer();
        saveTempVar(env, paramsStr);
        long currentTime = Bukkit.getWorlds().get(0).getTime();
        if (player != null) currentTime = player.getWorld().getTime();

        if (paramsStr.equalsIgnoreCase("day")) {
            return ((currentTime >= 0) && (currentTime < 12000));
        } else if (paramsStr.equalsIgnoreCase("night")) {
            return ((currentTime >= 12000) && (currentTime < 23999));

        } else {
            String[] tln = paramsStr.split(",");
            for (String timeStr : tln) {
                OptionalInt timeOpt = NumberUtils.parseInteger(timeStr, Is.NON_NEGATIVE);
                if (timeOpt.isPresent()) {
                    int ct = (int) ((currentTime / 1000 + 6) % 24);
                    if (ct == timeOpt.getAsInt()) return true;
                }
            }
        }
        return false;
    }

    private void saveTempVar(Environment env, String time) {
        StringBuilder result = new StringBuilder(time);
        if (!(time.equalsIgnoreCase("day") || time.equalsIgnoreCase("night"))) {
            String[] ln = time.split(",");
            if (ln.length > 0)
                for (int i = 0; i < ln.length; i++) {
                    OptionalInt tmpOpt = NumberUtils.parseInteger(ln[i], Is.NON_NEGATIVE);
                    if (tmpOpt.isEmpty()) continue;
                    String tmp = String.format("%02d:00", tmpOpt.getAsInt());
                    if (i == 0) result = new StringBuilder(tmp);
                    else result.append(", ").append(tmp);
                }
        }
        env.getVariables().set("time", result.toString());
    }
}

