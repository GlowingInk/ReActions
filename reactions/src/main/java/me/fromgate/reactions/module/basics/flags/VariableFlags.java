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

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class VariableFlags implements Flag, Aliased {
    private final Type flagType;
    private final boolean personal;

    public VariableFlags(Type flagType, boolean personal) {
        this.flagType = flagType;
        this.personal = personal;
    }

    @Override
    public @NotNull String getName() {
        return switch (flagType) {
            case EXIST -> personal ? "PLAYER_VAR_EXIST" : "GLOBAL_VAR_EXIST";
            case COMPARE -> personal ? "PLAYER_VAR_COMPARE" : "GLOBAL_VAR_COMPARE";
            case GREATER -> personal ? "PLAYER_VAR_GREATER" : "GLOBAL_VAR_GREATER";
            case LOWER -> personal ? "PLAYER_VAR_LOWER" : "GLOBAL_VAR_LOWER";
            case MATCH -> personal ? "PLAYER_VAR_MATCH" : "GLOBAL_VAR_MATCH";
        };
    }

    @Override
    public @NotNull Collection<String> getAliases() {
        return List.of(switch (flagType) {
            case EXIST -> personal ? "VAR_PLAYER_EXIST" : "VAR_EXIST";
            case COMPARE -> personal ? "VAR_PLAYER_COMPARE" : "VAR_COMPARE";
            case GREATER -> personal ? "VAR_PLAYER_GREATER" : "VAR_GREATER";
            case LOWER -> personal ? "VAR_PLAYER_LOWER" : "VAR_LOWER";
            case MATCH -> personal ? "VAR_PLAYER_MATCH" : "VAR_MATCH";
        });
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        String variableId;
        String value;
        String playerName = this.personal && (player != null) ? player.getName() : "";

        if (params.contains("id")) {
            variableId = params.getString("id", "");
            if (variableId.isEmpty()) return false;
            value = params.getString("value", "");
            playerName = params.getString("player", playerName);
        } else {
            String[] ln = params.origin().split("/", 2);
            if (ln.length == 0) return false;
            variableId = ln[0];
            value = (ln.length > 1) ? ln[1] : "";
        }
        if (playerName.isEmpty() && this.personal) return false;

        String variable = ReActions.getVariables().getVariable(playerName, variableId);
        if (variable == null) {
            return false;
        }

        switch (this.flagType) {
            case EXIST: // VAR_EXIST
                return true;

            case COMPARE: // VAR_COMPARE
                if (NumberUtils.isNumber(variable) && NumberUtils.isNumber(value)) {
                    return Double.parseDouble(variable) == Double.parseDouble(value);
                }
                return variable.equalsIgnoreCase(value);

            case GREATER: // VAR_GREATER
                return NumberUtils.asDouble(variable, 0) > NumberUtils.asDouble(value, 0);

            case LOWER: // VAR_LOWER
                return NumberUtils.asDouble(variable, 0) < NumberUtils.asDouble(value, 0);

            case MATCH: /* VAR_MATCH */ {
                try {
                    return variable.matches(value);
                } catch (PatternSyntaxException ex) {
                    return false;
                }
            }

            default:
                return false;
        }
    }

    public enum Type {
        EXIST, COMPARE, GREATER, LOWER, MATCH
    }
}