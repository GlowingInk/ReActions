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

package fun.reactions.events.listeners;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.events.PlayerMoveByBlockEvent;
import fun.reactions.events.PlayerStayEvent;
import fun.reactions.holders.PushBack;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoveListener implements Listener {

    private static final Map<UUID, Location> prevLocations = new HashMap<>();

    public static void init() {
        if (Cfg.playerMoveTaskUse) {
            Bukkit.getScheduler().runTaskTimer(ReActions.getPlugin(), () -> Bukkit.getOnlinePlayers().forEach(pl -> {
                Location from = prevLocations.get(pl.getUniqueId());
                Location to = pl.getLocation();
                if (to.getWorld() != from.getWorld()) from = null;
                processMove(pl, from, to);
                prevLocations.put(pl.getUniqueId(), to);
            }), 30, Cfg.playerMoveTaskTick);
        } else Bukkit.getPluginManager().registerEvents(new MoveListener(), ReActions.getPlugin());
    }

    private static void processMove(Player player, Location from, Location to) {
        PushBack.rememberLocations(player, from, to);
        PlayerEvent event = LocationUtils.equalsPositionally(from, to) ?
                new PlayerStayEvent(player, to) : new PlayerMoveByBlockEvent(player, to, from);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void initLocation(Player player) {
        if (Cfg.playerMoveTaskUse) {
            prevLocations.put(player.getUniqueId(), player.getLocation());
        }
    }

    public static void removeLocation(Player player) {
        prevLocations.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        processMove(event.getPlayer(), event.getFrom(), event.getTo());
    }

}