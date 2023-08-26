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
import fun.reactions.util.NumberUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
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

}
