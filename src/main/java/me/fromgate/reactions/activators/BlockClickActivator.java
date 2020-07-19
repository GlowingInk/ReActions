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

package me.fromgate.reactions.activators;

import me.fromgate.reactions.storages.BlockClickStorage;
import me.fromgate.reactions.storages.Storage;
import me.fromgate.reactions.util.enums.ClickType;
import me.fromgate.reactions.util.item.ItemUtil;
import me.fromgate.reactions.util.location.LocationUtil;
import me.fromgate.reactions.util.parameter.Param;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class BlockClickActivator extends Activator implements Locatable {
	private final Material blockType;
	private final String blockLocation;
	private final ClickType click;

	private BlockClickActivator(ActivatorBase base, Material block, String location, ClickType click) {
		super(base);
		this.blockType = block;
		this.blockLocation = location;
		this.click = click;
	}

	@Override
	public boolean activate(Storage event) {
		BlockClickStorage bce = (BlockClickStorage) event;
		if (bce.getBlock() == null) return false;
		if (!isActivatorBlock(bce.getBlock())) return false;
		if (click.checkRight(bce.isLeftClick())) return false;
		return true;
	}

	private boolean isActivatorBlock(Block block) {
		if (this.blockType != null && block.getType() != this.blockType) return false;
		return checkLocations(block);
	}

	private boolean checkLocations(Block block) {
		if (this.blockLocation.isEmpty()) return true;
		return this.isLocatedAt(block.getLocation());
	}


	@Override
	public boolean isLocatedAt(Location l) {
		if (this.blockLocation.isEmpty()) return false;
		// Location loc = Locator.parseCoordinates(this.blockLocation);
		Location loc = LocationUtil.parseLocation(this.blockLocation, null);
		if (loc == null) return false;
		return l.getWorld().equals(loc.getWorld()) &&
				l.getBlockX() == loc.getBlockX() &&
				l.getBlockY() == loc.getBlockY() &&
				l.getBlockZ() == loc.getBlockZ();
	}

	@Override
	public boolean isLocatedAt(World world, int x, int y, int z) {
		return isLocatedAt(new Location(world, x, y, z));
	}

	@Override
	public void save(ConfigurationSection cfg) {
		if(blockType != null) cfg.set("block-type", this.blockType.name());
		cfg.set("click-type", click.name());
		cfg.set("location", this.blockLocation.isEmpty() ? null : this.blockLocation);
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.BLOCK_CLICK;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" (");
		sb.append("type:").append(blockType == null ? "-" : blockType);
		sb.append("; click:").append(this.click.name());
		sb.append("; loc:").append(blockLocation.isEmpty() ? "-" : blockLocation);
		sb.append(")");
		return sb.toString();
	}

	public boolean isValid() {
		// return (this.blockType == null || this.blockType.isEmpty()) && (this.blockLocation == null || this.blockLocation.isEmpty());
		return true;
	}

	public static BlockClickActivator create(ActivatorBase base, Param param) {
		Material block = ItemUtil.getMaterial(param.getParam("block-type"));
		ClickType click = ClickType.getByName(param.getParam("click-type"));
		String loc = param.getParam("location");
		return new BlockClickActivator(base, block, loc, click);
	}

	public static BlockClickActivator load(ActivatorBase base, ConfigurationSection cfg) {
		Material block = ItemUtil.getMaterial(cfg.getString("block-type"));
		ClickType click = ClickType.getByName(cfg.getString("click-type"));
		String loc = cfg.getString("location");
		return new BlockClickActivator(base, block, loc, click);
	}

}