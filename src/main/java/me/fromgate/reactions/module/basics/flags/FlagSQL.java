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

import lombok.AllArgsConstructor;
import me.fromgate.reactions.SQLManager;
import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;

@AllArgsConstructor
public class FlagSQL implements OldFlag {
    // TODO: Make it safer
    private final boolean check;

    @Override
    public boolean checkFlag(RaContext context, String param) {
        if (!SQLManager.isEnabled()) return false;
        Parameters params = Parameters.fromString(param);
        if (!params.containsEvery("value", "select", "from") &&
                !(params.contains("query"))) return false;
        String value = params.getString("value", "");
        String select = params.getString("select", "");
        String query = params.getString("query", "");
        if (query.isEmpty()) {
            if (select.isEmpty()) return false;
            String from = params.getString("from", "");
            if (from.isEmpty()) return false;
            String where = params.getString("where", "");
            query = "SELECT " + select + " FROM " + from + (where.isEmpty() ? "" : " WHERE " + where);
        }
        int column = params.getInteger("column", 1);
        if (check) return SQLManager.compareSelect(value, query, column, params, context.getVariable("SQL_SET"));
        else return SQLManager.isSelectResultEmpty(query);
    }

}
