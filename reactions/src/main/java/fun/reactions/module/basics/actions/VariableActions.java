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

package fun.reactions.module.basics.actions;

import fun.reactions.ReActions;
import fun.reactions.logic.activity.actions.Action;
import fun.reactions.logic.environment.Environment;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class VariableActions implements Action, Aliased {

    private final Type actType;
    private final boolean personal;

    public VariableActions(Type actType, boolean personalVar) {
        this.actType = actType;
        this.personal = personalVar;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) { // TODO: There's a lot of room for improvements
        Parameters params = Parameters.fromString(paramsStr);
        Player p = env.getPlayer();

        String player = (p != null && this.personal) ? p.getName() : "";

        String var;
        String value;

        if (params.contains("id")) {
            var = params.getString("id");
            value = params.getString("value");
            player = params.getString("player", player);
            if (var.isEmpty()) return false;
        } else {
            String[] ln = params.origin().split("/", 2);
            if (ln.length == 0) return false;
            var = ln[0];
            value = (ln.length > 1) ? ln[1] : "";
        }

        if (this.personal && player.isEmpty()) return false;

        switch (this.actType) {
            case SET -> ReActions.getVariables().setVariable(player, var, value);
            case CLEAR -> ReActions.getVariables().removeVariable(player, var);
            case INCREASE, DECREASE -> {
                String variable = ReActions.getVariables().getVariable(player, var);
                if (variable == null || !NumberUtils.isNumber(variable)) return false;
                double variableValue = Double.parseDouble(variable);
                double mod = value.isEmpty() || !(NumberUtils.isNumber(value)) ? 1 : Double.parseDouble(value);
                variableValue += actType == Type.INCREASE ? mod : -mod;
                ReActions.getVariables().setVariable(player, var, NumberUtils.format(variableValue));
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
    public boolean requiresPlayer() {
        return false;
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
