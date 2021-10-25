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

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class FlagCompare extends Flag {
    @Override
    public boolean check(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String paramValue = params.getString("param", "");
        if (paramValue.isEmpty()) return false;
        if (!params.contains("value1")) return false;
        for (String valueKey : params.keySet()) {
            if (!((valueKey.toLowerCase(Locale.ENGLISH)).startsWith("value"))) continue;
            String value = params.getString(valueKey);
            if (NumberUtils.isIntegerSigned(value, paramValue) && (Integer.parseInt(value) == Integer.parseInt(paramValue)))
                return true;
            else if (paramValue.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "COMPARE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
