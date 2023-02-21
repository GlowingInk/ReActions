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

import fun.reactions.logic.activity.actions.Action;
import fun.reactions.logic.environment.Environment;
import fun.reactions.time.CooldownManager;
import fun.reactions.util.TimeUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class CooldownActions implements Action, Aliased {

    private final boolean global;

    public CooldownActions(boolean global) {
        this.global = global;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        String timeStr = "";
        String playerName = this.global ? "" : (player != null ? player.getName() : "");
        String variableId = "";
        boolean add = false;
        if (params.contains("id") && params.containsAny("delay", "time")) {
            variableId = params.getString("id");
            playerName = params.getString("player", playerName);
            timeStr = params.getStringSafe("delay", () -> params.getString("time"));
            add = params.getBoolean("add", false);
        } else { // TODO Remove legacy format
            String oldFormat = params.origin();
            if (oldFormat.contains("/")) {
                String[] m = oldFormat.split("/");
                if (m.length >= 2) {
                    timeStr = m[0];
                    variableId = m[1];
                }
            } else timeStr = oldFormat;
        }

        if (timeStr.isEmpty()) return false;
        if (variableId.isEmpty()) return false;
        setCooldown(playerName, variableId, TimeUtils.parseTime(timeStr), add);
        CooldownManager.setTempPlaceholders(env, playerName, variableId);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return global ? "COOLDOWN" : "COOLDOWN_PLAYER";
    }

    @Override
    public @NotNull Collection<@NotNull String> getAliases() {
        return List.of(global ? "DELAY" : "DELAY_PLAYER");
    }

    @Override
    public boolean requiresPlayer() {
        return !global;
    }

    private void setCooldown(String playerName, String variableId, long cdTime, boolean add) {
        if (playerName.isEmpty()) CooldownManager.setCooldown(variableId, cdTime, add);
        else CooldownManager.setPersonalCooldown(playerName, variableId, cdTime, add);
    }

}
