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
import me.fromgate.reactions.externals.worldguard.RaWorldGuard;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: WorldGuard module
@AllArgsConstructor
public class FlagRegion extends Flag {
    private final Type flagType;

    @Override
    protected boolean check(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        if (!RaWorldGuard.isConnected()) return false;
        String param = params.toString();
        return switch (flagType) {
            case REGION -> RaWorldGuard.isPlayerInRegion(player, param);
            case REGION_PLAYERS -> playersInRegion(param);
            case REGION_MEMBER -> RaWorldGuard.isMember(player, param);
            case REGION_OWNER -> RaWorldGuard.isOwner(player, param);
            case REGION_STATE -> RaWorldGuard.isFlagInRegion(player, param);
        };
    }

    private boolean playersInRegion(String param) {
        String[] split = param.split("/");
        if(split.length != 2) return false;
        return (NumberUtils.getInteger(split[1], 1) <= RaWorldGuard.countPlayersInRegion(split[0]));
    }

    @Override
    public @NotNull String getName() {
        return flagType.name();
    }

    @Override
    public boolean requiresPlayer() {
        return flagType == Type.REGION;
    }

    public enum Type {
        REGION, REGION_PLAYERS, REGION_MEMBER, REGION_OWNER, REGION_STATE
    }
}
