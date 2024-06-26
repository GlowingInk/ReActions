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

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.BlockUtils;
import fun.reactions.util.Utils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

@Aliased.Names("SIGN_LINE")
public class SignSetAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        // loc:world,x,y,z line1:text line2:text line3:text line4:text clear:1,2,3,4
        String locStr = params.getString("loc", env.getVariables().getString("sign_loc")); // TODO What, why?
        if (Utils.isStringEmpty(locStr)) return false;
        Location loc = LocationUtils.parseCoordinates(locStr);
        if (loc == null) return false;
        boolean chunkLoad = params.getBoolean("loadchunk", false);
        if (!chunkLoad && !loc.getChunk().isLoaded()) return false;
        Block block = loc.getBlock();
        if (!BlockUtils.isSign(block)) return false;
        Sign sign = (Sign) block.getState();
        for (int i = 1; i <= 4; i++) {
            String line = params.getString("line" + i, "");
            if (line.isEmpty()) continue;
            if (line.length() > 15) line = line.substring(0, 15);
            sign.setLine(i - 1, ChatColor.translateAlternateColorCodes('&', line));
        }

        String clear = params.getString("clear");
        if (!clear.isEmpty()) {
            String[] ln = clear.split(",");
            for (String cl : ln) {
                OptionalInt lineOpt = NumberUtils.parseInteger(cl, Is.inRange(0, 4));
                if (lineOpt.isEmpty()) continue;
                sign.setLine(lineOpt.getAsInt(), "");
            }
        }
        sign.update(true);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "SIGN_SET_LINE";
    }

}
