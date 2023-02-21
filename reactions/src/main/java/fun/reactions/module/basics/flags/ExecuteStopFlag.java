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

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.message.Msg;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("EXEC_STOP")
@Deprecated
public class ExecuteStopFlag implements Flag {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        // TODO Custom ActivatorType to handle exec stopping
        Player player = env.getPlayer();
        Msg.logOnce("flagexecstopnotworking", "Sorry, but flag EXECUTE_STOP doesn't work yet.");
        return false; // ReActions.getActivators().isStopped(player, param, false);
    }

    @Override
    public @NotNull String getName() {
        return "EXECUTE_STOP";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
