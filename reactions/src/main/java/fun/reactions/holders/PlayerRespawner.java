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

package fun.reactions.holders;

import fun.reactions.ReActions;
import fun.reactions.module.basic.activators.RespawnActivator;
import fun.reactions.util.enums.DeathCause;
import fun.reactions.util.mob.EntityUtils;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TODO: Move to LocationHolder
public final class PlayerRespawner {
    private static final Map<UUID, LivingEntity> players = new HashMap<>();
    private static final Map<UUID, Location> deathPoints = new HashMap<>();

    private PlayerRespawner() {}

    public static void addPlayerRespawn(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        deathPoints.put(deadPlayer.getUniqueId(), deadPlayer.getLocation());
        LivingEntity killer = EntityUtils.getKillerEntity(deadPlayer.getLastDamageCause());
        players.put(deadPlayer.getUniqueId(), killer);
    }

    public static Location getLastDeathPoint(Player player) {
        return deathPoints.getOrDefault(player.getUniqueId(), player.getLocation());
    }

    public static void triggerPlayerRespawn(Player player, Location respawnLoc) {
        if (!players.containsKey(player.getUniqueId())) return;
        LivingEntity killer = players.remove(player.getUniqueId());
        DeathCause d = killer == null ?
                DeathCause.OTHER :
                killer.getType() == EntityType.PLAYER ? DeathCause.PVP : DeathCause.PVE;
        ReActions.getActivators().activate(new RespawnActivator.Context(player, killer, d, respawnLoc));
    }

}
