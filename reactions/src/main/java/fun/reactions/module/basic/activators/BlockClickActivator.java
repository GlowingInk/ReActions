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

package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.variables.BlockVariable;
import fun.reactions.util.Utils;
import fun.reactions.util.block.VirtualBlockData;
import fun.reactions.util.enums.ClickType;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

// TODO Add Hand
public class BlockClickActivator extends Activator implements Locatable {
    private final VirtualBlockData blockData;
    private final String blockLocation;
    private final ClickType click;

    private BlockClickActivator(Logic base, VirtualBlockData block, String location, ClickType click) {
        super(base);
        this.blockData = block;
        this.blockLocation = location;
        this.click = click;
    }

    public static BlockClickActivator create(Logic base, Parameters param) {
        VirtualBlockData block = param.get("block-type", VirtualBlockData::fromString);
        ClickType click = param.getSafe("click-type", ClickType::getByName);
        String loc = param.getString("location");
        return new BlockClickActivator(base, block, loc, click);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context bce = (Context) context;
        if (bce.block == null) return false;
        if (!isActivatorBlock(bce.block)) return false;
        return click.checkRight(!bce.leftClick);
    }

    private boolean isActivatorBlock(Block block) {
        if (this.blockData != null && !blockData.matches(block)) return false;
        return checkLocations(block);
    }

    private boolean checkLocations(Block block) {
        if (Utils.isStringEmpty(blockLocation)) return true;
        return this.isLocatedAt(block.getLocation());
    }

    public boolean isLocatedAt(Location l) {
        if (this.blockLocation.isEmpty()) return false;
        // Location loc = Locator.parseCoordinates(this.blockLocation);
        Location loc = LocationUtils.parseLocation(this.blockLocation, null);
        if (loc == null) return false;
        return l.getWorld().equals(loc.getWorld()) &&
                l.getBlockX() == loc.getBlockX() &&
                l.getBlockY() == loc.getBlockY() &&
                l.getBlockZ() == loc.getBlockZ();
    }

    @Override
    public boolean isLocatedAt(@NotNull World world, int x, int y, int z) {
        return isLocatedAt(new Location(world, x, y, z));
    }

    @Override
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("block-type", blockData == null ? null : blockData.asString());
        cfg.set("click-type", click.name());
        cfg.set("location", Utils.isStringEmpty(blockLocation) ? null : blockLocation);
    }

    /*
    public boolean isValid() {
        return (this.blockType == null || this.blockType.isEmpty()) && (this.blockLocation == null || this.blockLocation.isEmpty());
    }
    */

    public static class Context extends ActivationContext {
        private final Block block;
        private final boolean leftClick;

        public Context(Player p, Block block, boolean leftClick) {
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
            Map<String, Variable> vars = new HashMap<>(BlockVariable.flatVars(block));
            vars.put(CANCEL_EVENT, Variable.value(false));
            return vars;
        }
    }
}
