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

import fun.reactions.logic.activity.ActivitiesRegistry;
import fun.reactions.logic.activity.flags.Flag;
import fun.reactions.logic.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.ParametersUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Aliased.Names({"FLAGS_OR", "FLAG_SET"})
public class EitherFlag implements Flag {
    private final ActivitiesRegistry registry;

    public EitherFlag(ActivitiesRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        List<String> split = ParametersUtils.splitSafely(params, ' ');
        boolean hasPlayer = env.getPlayer() != null;
        for (String flagFullStr : split) {
            String[] flagSplit = flagFullStr.split(":", 2);
            if (flagSplit.length == 1) {
                continue;
            }
            if (flagSplit[1].startsWith("{") && flagSplit[1].endsWith("}")) {
                flagSplit[1] = flagSplit[1].substring(1, flagSplit.length - 1);
            }
            boolean invert = flagSplit[0].startsWith("!");
            Flag flag = registry.getFlag(invert ? flagSplit[0].substring(1) : flagSplit[0]);
            if (flag != null && (!flag.requiresPlayer() || hasPlayer) && invert != flag.proceed(env, flagSplit[1])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "EITHER";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
