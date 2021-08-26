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

import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.time.TimersManager;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class FlagTimerActive extends Flag {
    @Override
    protected boolean check(@NotNull RaContext context, @NotNull Parameters params) {
        return TimersManager.isTimerWorking(params.toString());
    }

    @Override
    public @NotNull String getName() {
        return "TIMER_ACTIVE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected boolean isParameterized() {
        return false;
    }
}