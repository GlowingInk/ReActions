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
import fun.reactions.module.basics.activators.BlockClickActivator;
import fun.reactions.util.location.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static fun.reactions.model.environment.Variable.simple;

public class BlockClickContext extends ActivationContext {

    private final Block block;
    private final boolean leftClick;

    public BlockClickContext(Player p, Block block, boolean leftClick) {
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
                CANCEL_EVENT, Variable.property(false),
                "blocklocation", simple(LocationUtils.locationToString(block.getLocation())),
                "blocktype", Variable.simple(block.getType()),
                "block", Variable.simple(block.getType()) // FIXME Why there is a copy?
        );
    }

    public Block getBlock() {return this.block;}

    public boolean isLeftClick() {return this.leftClick;}
}
