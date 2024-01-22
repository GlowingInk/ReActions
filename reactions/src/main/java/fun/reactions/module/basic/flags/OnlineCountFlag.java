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

package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

@Aliased.Names("ONLINE")
public class OnlineCountFlag implements Flag {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        OptionalInt countOpt = NumberUtils.parseInteger(paramsStr, Is.POSITIVE);
        return countOpt.isPresent() && env.getPlatform().getServer().getOnlinePlayers().size() >= countOpt.getAsInt();
    }

    @Override
    public @NotNull String getName() {
        return "ONLINE_COUNT";
    }

}
