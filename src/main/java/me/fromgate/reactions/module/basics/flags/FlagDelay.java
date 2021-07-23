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
import me.fromgate.reactions.logic.activity.flags.OldFlag;
import me.fromgate.reactions.time.Delayer;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class FlagDelay implements OldFlag {

    private final boolean globalDelay;

    @Override
    public boolean checkFlag(RaContext context, String param) {
        Player player = context.getPlayer();
        String playerName = this.globalDelay ? "" : (player != null ? player.getName() : "");
        long updateTime = 0;
        String id = param;

        Parameters params = Parameters.fromString(param);
        if (params.contains("id")) {
            id = params.getString("id");
            updateTime = TimeUtils.parseTime(params.getString("set-delay", params.getString("set-time", "0")));
            playerName = params.getString("player", playerName);
        }
        boolean result = playerName.isEmpty() ? Delayer.checkDelay(id, updateTime) : Delayer.checkPersonalDelay(playerName, id, updateTime);
        Delayer.setTempPlaceholders(context, playerName, id);
        return result;
    }

}
