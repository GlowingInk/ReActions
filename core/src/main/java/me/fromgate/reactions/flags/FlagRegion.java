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

package me.fromgate.reactions.flags;

import me.fromgate.reactions.externals.RaWorldGuard;
import me.fromgate.reactions.util.Param;
import org.bukkit.entity.Player;

public class FlagRegion extends Flag {

    final static int REGION = 0;
    final static int REGION_PLAYERS = 1;
    final static int REGION_MEMBER = 2;
    final static int REGION_OWNER = 3;
    final static int REGION_STATE = 4;

    private int flagType = 0;

    public FlagRegion(int flagType) {
        this.flagType = flagType;
    }

    @Override
    public boolean checkFlag(Player player, String param) {
        if (!RaWorldGuard.isConnected()) return false;
        switch (flagType) {
            case REGION:
                return RaWorldGuard.isPlayerInRegion(player, param);
            case REGION_PLAYERS:
                return playersInRegion(param);
            case REGION_MEMBER:
                return RaWorldGuard.isPlayerIsMember(player, param);
            case REGION_OWNER:
                return RaWorldGuard.isPlayerIsOwner(player, param);
            case REGION_STATE:
                return RaWorldGuard.isFlagInRegion(player, param);
        }
        return false;
    }

    private boolean playersInRegion(String param) {
        Param params = Param.fromOldFormat(param, "/", "region", "players");
        String rg = params.getParam("region");
        int minp = params.getParam("players", 1);
        return (minp <= RaWorldGuard.countPlayersInRegion(rg));
    }
}
