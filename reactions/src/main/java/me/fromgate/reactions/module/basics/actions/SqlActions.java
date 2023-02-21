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

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.SQLManager;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SqlActions implements Action {
    // TODO: More functionality like working with arrays
    private final Type sqlType;

    public SqlActions(Type sqlType) {
        this.sqlType = sqlType;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String playerName = params.getString("player");
        String varName = params.getString("variable");
        int column = params.getInteger("column", 1);
        String query = params.getString("query").trim();
        switch (sqlType) {
            case SELECT -> { // SELECT to variable
                if (query.isEmpty()) return false;
                if (!query.toLowerCase(Locale.ROOT).startsWith("select")) {
                    Msg.logOnce("needselect" + query, "You need to use only \"SELECT\" query in SQL_SELECT action. Query: " + query);
                    return false;
                }
                if (varName.isEmpty()) return false;
                ReActions.getVariables().setVariable(playerName, varName, SQLManager.executeSelect(query, column, params, env.getVariables().getString("SQL_SET")));
            }
            case INSERT -> { // INSERT
                query = params.getString("query", params.origin()).trim();
                if (query.isEmpty()) return false;
                if (!query.toLowerCase(Locale.ROOT).startsWith("insert")) {
                    Msg.logOnce("needinsert" + query, "You need to use only \"INSERT\" query in SQL_INSERT action. Query: " + query);
                    return false;
                }
                SQLManager.executeUpdate(query, params);
            }
            case UPDATE -> { // UPDATE
                query = params.getString("query", params.origin()).trim();
                if (query.isEmpty()) return false;
                if (!query.toLowerCase(Locale.ROOT).startsWith("update")) {
                    Msg.logOnce("needupdate" + query, "You need to use only \"UPDATE\" query in SQL_UPDATE action. Query: " + query);
                    return false;
                }
                SQLManager.executeUpdate(query, params);
            }
            case DELETE -> { // DELETE
                query = params.getString("query", params.origin()).trim();
                if (query.isEmpty()) return false;
                if (!query.toLowerCase(Locale.ROOT).startsWith("delete")) {
                    Msg.logOnce("needdelete" + query, "You need to use only \"DELETE\" query in SQL_DELETE action. Query: " + query);
                    return false;
                }
                SQLManager.executeUpdate(query, params);
            }
            case SET -> { // SET
                query = params.getString("query", params.origin()).trim();
                if (query.isEmpty()) return false;
                if (!query.toLowerCase(Locale.ROOT).startsWith("set")) {
                    Msg.logOnce("needset" + query, "You need to use only \"SET\" query in SQL_SET action. Query: " + query);
                    return false;
                }
                env.getVariables().set("SQL_SET", query);
            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "SQL_" + sqlType.name();
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    public enum Type {
        SELECT, INSERT, UPDATE, DELETE, SET
    }
}
