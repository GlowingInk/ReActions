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

package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.Locatable;
import fun.reactions.module.basics.contexts.BlockClickContext;
import fun.reactions.util.Utils;
import fun.reactions.util.enums.ClickType;
import fun.reactions.util.item.ItemUtils;
import fun.reactions.util.location.LocationUtils;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

// TODO Add Hand
public class BlockClickActivator extends Activator implements Locatable {
    private final Material blockType;
    private final String blockLocation;
    private final ClickType click;

    private BlockClickActivator(Logic base, Material block, String location, ClickType click) {
        super(base);
        this.blockType = block;
        this.blockLocation = location;
        this.click = click;
    }

    public static BlockClickActivator create(Logic base, Parameters param) {
        Material block = ItemUtils.getMaterial(param.getString("block-type"));
        ClickType click = ClickType.getByName(param.getString("click-type"));
        String loc = param.getString("location");
        return new BlockClickActivator(base, block, loc, click);
    }

    public static BlockClickActivator load(Logic base, ConfigurationSection cfg) {
        Material block = ItemUtils.getMaterial(cfg.getString("block-type", ""));
        ClickType click = ClickType.getByName(cfg.getString("click-type", ""));
        String loc = cfg.getString("location");
        return new BlockClickActivator(base, block, loc, click);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        BlockClickContext bce = (BlockClickContext) context;
        if (bce.getBlock() == null) return false;
        if (!isActivatorBlock(bce.getBlock())) return false;
        return click.checkRight(!bce.isLeftClick());
    }

    private boolean isActivatorBlock(Block block) {
        if (this.blockType != null && block.getType() != this.blockType) return false;
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
        cfg.set("block-type", blockType == null ? null : blockType.name());
        cfg.set("click-type", click.name());
        cfg.set("location", Utils.isStringEmpty(blockLocation) ? null : blockLocation);
    }

    @Override
    public String toString() {
        return super.toString() + " (" +
                "type:" + (blockType == null ? "-" : blockType) +
                "; click:" + this.click.name() +
                "; loc:" + (blockLocation == null ? "-" : blockLocation) +
                ")";
    }

    /*
    public boolean isValid() {
        return (this.blockType == null || this.blockType.isEmpty()) && (this.blockLocation == null || this.blockLocation.isEmpty());
    }
    */
}
