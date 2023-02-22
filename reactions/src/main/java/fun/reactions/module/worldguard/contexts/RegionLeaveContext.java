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

package fun.reactions.module.worldguard.contexts;

import fun.reactions.model.activators.Activator;
import fun.reactions.module.worldguard.activators.RegionLeaveActivator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegionLeaveContext extends RegionContext {
    public RegionLeaveContext(Player player, String region) {
        super(player, region);
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return RegionLeaveActivator.class;
    }
}
