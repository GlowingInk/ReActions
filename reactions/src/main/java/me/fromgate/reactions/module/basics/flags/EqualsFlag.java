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
import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"EQUALITY", "EQUAL", "COMPARE"})
public class EqualsFlag implements Flag { // TODO Rewrite the check
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String paramValue = params.getString("param", null);
        if (paramValue == null) return false;
        Double paramNum = NumberUtils.isNumber(paramValue) ? Double.valueOf(paramValue) : null;
        boolean caseSensitive = params.getBoolean("case-sensitive");
        for (String key : params.keyedList("value")) {
            String value = params.getString(key);
            if (
                    (caseSensitive ? paramValue.equals(value) : paramValue.equalsIgnoreCase(value)) ||
                    (paramNum != null && NumberUtils.isNumber(value) && Double.parseDouble(value) == paramNum)
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "EQUALS";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
