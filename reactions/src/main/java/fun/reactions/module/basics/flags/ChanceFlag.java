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
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import fun.reactions.util.Rng;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("PROBABILITY")
public class ChanceFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        env.getVariables().set("chance", content + "%");
        double d = 50;
        if (NumberUtils.isNumber(content, Is.POSITIVE)) d = Double.parseDouble(content);
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
