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

import fun.reactions.PersistentVariablesManager;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class PersistentVarActions implements Action, Aliased {

    private final Type actType;
    private final boolean personal;

    public PersistentVarActions(Type actType, boolean personalVar) {
        this.actType = actType;
        this.personal = personalVar;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) { // TODO: There's a lot of room for improvements
        Parameters params = Parameters.fromString(paramsStr);
        String playerName = (env.getPlayer() != null && this.personal) ? env.getPlayer().getName() : "";

        String varName;
        String change;

        if (params.contains("id")) {
            varName = params.getString("id");
            change = params.getString("value");
            playerName = params.getString("player", playerName);
            if (varName.isEmpty()) return false;
        } else {
            String[] ln = params.origin().split("/", 2);
            if (ln.length == 0) return false;
            varName = ln[0];
            change = (ln.length > 1) ? ln[1] : "";
        }

        if (this.personal && playerName.isEmpty()) return false;

        PersistentVariablesManager varsManager = env.getPlatform().getPersistentVariables();
        switch (this.actType) {
            case SET -> varsManager.setVariable(playerName, varName, change);
            case CLEAR -> varsManager.removeVariable(playerName, varName);
            case INCREASE, DECREASE -> {
                String varValue = varsManager.getVariable(playerName, varName);
                double varNumberValue;
                if (varValue == null) {
                    varNumberValue = 0;
                } else if (NumberUtils.isNumber(varValue)) {
                    varNumberValue = Double.parseDouble(varValue);
                } else {
                    return false;
                }
                double mod = change.isEmpty() || !(NumberUtils.isNumber(change)) ? 1 : Double.parseDouble(change);
                varNumberValue += actType == Type.INCREASE ? mod : -mod;
                varsManager.setVariable(playerName, varName, NumberUtils.format(varNumberValue));

            }
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return switch (actType) {
            case SET -> personal ? "PLAYER_VAR" : "GLOBAL_VAR";
            case CLEAR -> personal ? "PLAYER_VAR_CLEAR" : "GLOBAL_VAR_CLEAR";
            case INCREASE -> personal ? "PLAYER_VAR_INC" : "GLOBAL_VAR_INC";
            case DECREASE -> personal ? "PLAYER_VAR_DEC" : "GLOBAL_VAR_DEC";
        };
    }

    @Override
    public @NotNull Collection<@NotNull String> getAliases() {
        return switch (actType) {
            case SET -> personal ? List.of("VAR_PLAYER_SET", "PLAYER_VAR_SET") : List.of("VAR_SET", "GLOBAL_VAR_SET");
            case CLEAR -> List.of(personal ? "VAR_PLAYER_CLEAR" : "VAR_CLEAR");
            case INCREASE -> List.of(personal ? "VAR_PLAYER_INC" : "VAR_INC");
            case DECREASE -> List.of(personal ? "VAR_PLAYER_DEC" : "VAR_DEC");
        };
    }

    public enum Type {
        SET, CLEAR, INCREASE, DECREASE
    }
}
