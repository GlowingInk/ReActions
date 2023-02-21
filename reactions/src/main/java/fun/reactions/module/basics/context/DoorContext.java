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

package fun.reactions.module.basics.context;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.basics.activators.DoorActivator;
import fun.reactions.util.BlockUtils;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DoorContext extends ActivationContext {
    public static final String DOOR_LOCATION = "door_loc";

    private final Block doorBlock;

    public DoorContext(Player p, Block block) {
        super(p);
        this.doorBlock = block;
    }

    public boolean isDoorOpened() {
        return BlockUtils.isOpen(doorBlock);
    }

    public Location getDoorLocation() {
        return doorBlock.getLocation();
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return DoorActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, Variable.property(false),
                DOOR_LOCATION, Variable.simple(LocationUtils.locationToString(doorBlock))
        );
    }

    public Block getDoorBlock() {return this.doorBlock;}
}
