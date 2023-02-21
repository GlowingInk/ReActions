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

import fun.reactions.logic.activity.flags.Flag;
import fun.reactions.logic.environment.Environment;
import fun.reactions.time.CooldownManager;
import fun.reactions.util.TimeUtils;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class CooldownFlags implements Flag, Aliased {

    private final boolean globalDelay;

    public CooldownFlags(boolean globalDelay) {
        this.globalDelay = globalDelay;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        String playerName = this.globalDelay ? "" : (player != null ? player.getName() : "");
        long updateTime = 0;
        String id = params.origin();
        if (params.contains("id")) {
            id = params.getString("id");
            updateTime = TimeUtils.parseTime(params.getString("set-delay", params.getString("set-time", "0")));
            playerName = params.getString("player", playerName);
        }
        boolean result = playerName.isEmpty() ? CooldownManager.checkCooldown(id, updateTime) : CooldownManager.checkPersonalCooldown(playerName, id, updateTime);
        CooldownManager.setTempPlaceholders(env, playerName, id);
        return result;
    }

    @Override
    public @NotNull String getName() {
        return globalDelay ? "COOLDOWN" : "COOLDOWN_PLAYER";
    }

    @Override
    public @NotNull Collection<@NotNull String> getAliases() {
        return List.of(globalDelay ? "DELAY" : "DELAY_PLAYER");
    }

    @Override
    public boolean requiresPlayer() {
        return !globalDelay;
    }
}
