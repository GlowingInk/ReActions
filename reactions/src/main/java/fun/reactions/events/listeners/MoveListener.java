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

import fun.reactions.cfg.RaConfiguration;
import fun.reactions.cfg.Reloadable;
import fun.reactions.events.MovementHandler;
import fun.reactions.holders.PushBack;
import fun.reactions.module.basic.ContextManager;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoveListener implements Listener, Reloadable {
    private final Map<UUID, Location> prevLocations = new HashMap<>();
    private final Plugin plugin;
    private final MovementHandler movement;

    private boolean useTask;
    private int taskTick;

    public MoveListener(@NotNull Plugin plugin, @NotNull MovementHandler movement) {
        this.plugin = plugin;
        this.movement = movement;
    }

    @Override
    public void acceptReload(@NotNull RaConfiguration config) {
        useTask = config.generalCfg().playerMoveEvent().use();
        taskTick = config.generalCfg().playerMoveEvent().taskTick();
    }

    public void init() {
        if (useTask) {
            plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Location from = prevLocations.get(player.getUniqueId());
                    Location to = player.getLocation();
                    if (from != null && to.getWorld() != from.getWorld()) from = null;
                    processMove(player, from, to);
                    prevLocations.put(player.getUniqueId(), to);
                }
            }, 30, taskTick);
        } else {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private void processMove(@NotNull Player player, @Nullable Location from, @NotNull Location to) {
        PushBack.rememberLocations(player, from, to);
        if (from == null || !LocationUtils.equalsPositionally(from, to)) {
            movement.onMove(player, from, to);
        }
        ContextManager.triggerCuboid(player);
    }

    public void initLocation(@NotNull Player player) {
        if (useTask) {
            prevLocations.put(player.getUniqueId(), player.getLocation());
        }
    }

    public void removeLocation(@NotNull Player player) {
        prevLocations.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        processMove(event.getPlayer(), event.getFrom(), event.getTo());
    }
}
