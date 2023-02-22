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

package fun.reactions.module.worldedit.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldedit.external.RaWorldEdit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: WorldEdit module
/**
 * Created by MaxDikiy on 11/10/2017.
 */
public class WeSuperPickaxeFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        Player player = env.getPlayer();
        return Boolean.parseBoolean(params) == RaWorldEdit.hasSuperPickAxe(player);
    }

    @Override
    public @NotNull String getName() {
        return "WE_SUPERPICKAXE";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
