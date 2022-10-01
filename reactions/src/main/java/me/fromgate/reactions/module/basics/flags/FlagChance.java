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

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.Rng;
import me.fromgate.reactions.util.alias.Aliases;
import org.jetbrains.annotations.NotNull;

@Aliases("PROBABILITY")
public class FlagChance implements Flag {
    @Override
    public boolean check(@NotNull RaContext context, @NotNull String params) {
        context.setVariable("chance", params + "%");
        double d = 50;
        if (NumberUtils.isFloat(params)) d = Double.parseDouble(params);
        d = Math.max(Math.min(d, 100), 0);
        return Rng.percentChance(d);
    }

    @Override
    public @NotNull String getName() {
        return "CHANCE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}