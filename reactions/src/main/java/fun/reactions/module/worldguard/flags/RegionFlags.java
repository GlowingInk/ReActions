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

package fun.reactions.module.worldguard.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldguard.external.RaWorldGuard;
import fun.reactions.util.NumberUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: WorldGuard module
public class RegionFlags implements Flag {
    private final Type flagType;

    public RegionFlags(Type flagType) {
        this.flagType = flagType;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Player player = env.getPlayer();
        return switch (flagType) {
            case REGION -> RaWorldGuard.isPlayerInRegion(player, content);
            case REGION_PLAYERS -> playersInRegion(content);
            case REGION_MEMBER -> RaWorldGuard.isMember(player, content);
            case REGION_OWNER -> RaWorldGuard.isOwner(player, content);
            case REGION_STATE -> RaWorldGuard.isFlagInRegion(player, content);
        };
    }

    private boolean playersInRegion(String param) {
        String[] split = param.split("/");
        if (split.length != 2) return false;
        return (NumberUtils.asInteger(split[1], 1) <= RaWorldGuard.countPlayersInRegion(split[0]));
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
