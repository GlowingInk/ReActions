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
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionVar implements Action {

    private final Type actType;
    private final boolean personalVar;

    public ActionVar(Type actType, boolean personalVar) {
        this.actType = actType;
        this.personalVar = personalVar;
    }

    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String paramsStr) { // TODO: There's a lot of room for improvements
        Parameters params = Parameters.fromString(paramsStr);
        Player p = context.getPlayer();

        String player = (p != null && this.personalVar) ? p.getName() : "";

        String var;
        String value;

        if (params.contains("id")) {
            var = params.getString("id");
            value = params.getString("value");
            player = params.getString("player", player);
            if (var.isEmpty()) return false;
        } else {
            String[] ln = params.getString(Parameters.ORIGIN_KEY, "").split("/", 2);
            if (ln.length == 0) return false;
            var = ln[0];
            value = (ln.length > 1) ? ln[1] : "";
        }

        if (this.personalVar && player.isEmpty()) return false;

        switch (this.actType) {
            case SET -> ReActions.getVariables().setVariable(player, var, value);   //VAR_SET, VAR_PLAYER_SET
            case TEMPORARY_SET -> context.setVariable(var, value);                  //VAR_TEMP_SET
            case CLEAR -> ReActions.getVariables().removeVariable(player, var);     //VAR_CLEAR, VAR_PLAYER_CLEAR
            case INCREASE, DECREASE -> {                                            //VAR_INC, VAR_PLAYER_INC, VAR_DEC, VAR_PLAYER_DEC
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
            case SET -> personalVar ? "VAR_PLAYER_SET" : "VAR_SET";
            case CLEAR -> personalVar ? "VAR_PLAYER_CLEAR" : "VAR_CLEAR";
            case INCREASE -> personalVar ? "VAR_PLAYER_INC" : "VAR_INC";
            case DECREASE -> personalVar ? "VAR_PLAYER_DEC" : "VAR_DEC";
            case TEMPORARY_SET -> "VAR_TEMP_SET";
        };
    }

    @Override
    public boolean requiresPlayer() {
        return personalVar;
    }

    public enum Type {
        SET, CLEAR, INCREASE, DECREASE, TEMPORARY_SET
    }
}
