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

package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.BlockClickActivator;
import me.fromgate.reactions.util.location.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.plain;
import static me.fromgate.reactions.logic.context.Variable.property;

public class BlockClickDetails extends Details {

    private final Block block;
    private final boolean leftClick;

    public BlockClickDetails(Player p, Block block, boolean leftClick) {
        super(p);
        this.block = block;
        this.leftClick = leftClick;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return BlockClickActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        return Map.of(
                CANCEL_EVENT, property(false),
                "blocklocation", plain(LocationUtils.locationToString(block.getLocation())),
                "blocktype", plain(block.getType()),
                "block", plain(block.getType()) // FIXME Why there is a copy?
        );
    }

    public Block getBlock() {return this.block;}

    public boolean isLeftClick() {return this.leftClick;}
}
