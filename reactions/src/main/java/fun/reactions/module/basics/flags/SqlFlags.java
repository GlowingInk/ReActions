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

package fun.reactions.module.basics.flags;

import fun.reactions.SQLManager;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class SqlFlags implements Flag {
    // TODO: Make it safer
    private final boolean check;

    public SqlFlags(boolean check) {
        this.check = check;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        if (!SQLManager.isEnabled()) return false;
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
        if (check) return SQLManager.compareSelect(value, query, column, params, env.getVariables().getString("SQL_SET"));
        else return SQLManager.isSelectResultEmpty(query);
    }

    @Override
    public @NotNull String getName() {
        return check ? "SQL_CHECK" : "SQL_RESULT";
    }
}
