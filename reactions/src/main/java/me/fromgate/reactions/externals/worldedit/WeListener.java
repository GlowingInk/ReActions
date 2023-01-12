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

package me.fromgate.reactions.externals.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.module.basics.details.WeChangeDetails;
import me.fromgate.reactions.module.basics.details.WeSelectionRegionDetails;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static me.fromgate.reactions.externals.worldedit.RaWorldEdit.*;

public class WeListener {
    private static Region regionSelection = null;

    public static boolean triggerChangeSelectionRegion(Player player, Region selection, Region region) {
        WeSelection weSelection = new WeSelection(getRegionSelector(player).getTypeName(),
                BukkitAdapter.adapt(player.getWorld(), selection.getMinimumPoint()),
                BukkitAdapter.adapt(player.getWorld(), selection.getMaximumPoint()),
                selection.getArea(), BukkitAdapter.adapt(selection.getWorld()), region.toString());
        WeSelectionRegionDetails e = new WeSelectionRegionDetails(player, weSelection);
        ReActions.getActivators().activate(e);
        var changeables = e.getChangeables();
        return changeables != null && changeables.get(Details.CANCEL_EVENT) != null && changeables.get(Details.CANCEL_EVENT).asBoolean();
    }

    public static boolean triggerWEChange(Player player, Location location, Material blockType) {
        WeChangeDetails e = new WeChangeDetails(player, location, blockType);
        ReActions.getActivators().activate(e);
        var changeables = e.getChangeables();
        return changeables != null && changeables.get(Details.CANCEL_EVENT) != null && changeables.get(Details.CANCEL_EVENT).asBoolean();
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            Player player = Bukkit.getPlayer(actor.getUniqueId());
            Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
                Region selection = getSelection(player);
                if (selection != null) {
                    Region region;
                    try {
                        region = getRegion(player);
                        if (region != null) {
                            // Check Region Selection
                            checkChangeSelectionRegion(player, selection, region);
                        }
                    } catch (IncompleteRegionException ignore) {
                    }
                }
            }, 2);

            if (event.getStage() == EditSession.Stage.BEFORE_CHANGE) {
                event.setExtent(new WeDelegateExtent(actor, event.getExtent()));
            }
        }
    }

    @Subscribe
    public void onPlayerInput(PlayerInputEvent event) throws IncompleteRegionException {
        Player player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
        if (player != null) {
            Region selection = getSelection(player);
            if (selection != null) {
                Region region = getRegion(player);
                // Check Region Selection
                checkChangeSelectionRegion(player, selection, region);
            }
        }
    }

    private void checkChangeSelectionRegion(Player player, Region selection, Region region) {
        if (regionSelection == null || region != null && !region.toString().equals(regionSelection.toString())) {
            regionSelection = region.clone();
            if (triggerChangeSelectionRegion(player, selection, regionSelection)) {
                regionSelection = null;
                RegionSelector rs = getRegionSelector(player);
                if (rs != null) rs.clear();
            }
        }
    }

}


