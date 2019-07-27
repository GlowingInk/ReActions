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

import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.storage.BlockClickStorage;
import me.fromgate.reactions.storage.RAStorage;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import me.fromgate.reactions.util.item.ItemUtil;
import me.fromgate.reactions.util.location.Locator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class BlockClickActivator extends Activator implements Locatable {
	private Material blockType;
	private String blockLocation;
	private ClickType click;


	public BlockClickActivator(String name, Block targetBlock, String param) {
		super(name, "activators");
		this.blockLocation = "";
		this.blockType = null;
		/*
		if (targetBlock != null && blockLocation != null && !blockLocation.isEmpty()) {
			blockLocation = Locator.locationToString(targetBlock.getLocation());
		}
		this.blockType = param;
		Param params = new Param(param);
		this.blockType = params.getParam("type", "");
		this.blockLocation = params.getParam("loc", "");
		this.click = ClickType.getByName(params.getParam("click", "ANY"));
		*/
		Param params = new Param(param);
		if (targetBlock != null) {
			this.blockLocation = Locator.locationToString(targetBlock.getLocation());
			this.blockType = targetBlock.getType();
		}
		String bt = params.getParam("type", "");
		if (this.blockType == null || this.blockType == Material.AIR || !bt.isEmpty() && !this.blockType.name().equalsIgnoreCase(bt)) {
			this.blockType = ItemUtil.getMaterial(bt);
			this.blockLocation = params.getParam("loc", "");
		}
		this.click = ClickType.getByName(params.getParam("click", "ANY"));
	}

	public BlockClickActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
	}


	@Override
	public boolean activate(RAStorage event) {
		BlockClickStorage bce = (BlockClickStorage) event;
		if (bce.getBlock() == null) return false;
		if (!isActivatorBlock(bce.getBlock())) return false;
		if (!clickCheck(bce.isLeftClick())) return false;
		Variables.setTempVar("blocklocation", Locator.locationToString(bce.getBlock().getLocation()));
		Variables.setTempVar("blocktype", bce.getBlock().getType().name());
		Variables.setTempVar("click", bce.isLeftClick() ? "left" : "right");
		return Actions.executeActivator(bce.getPlayer(), this);
	}

	private boolean checkLocations(Block block) {
		if (this.blockLocation.isEmpty()) return true;
		return this.isLocatedAt(block.getLocation());
	}

	private boolean isActivatorBlock(Block block) {
		if (this.blockType != null && block.getType() != this.blockType) return false;
		return checkLocations(block);
	}


	@Override
	public boolean isLocatedAt(Location l) {
		if (this.blockLocation.isEmpty()) return false;
		// Location loc = Locator.parseCoordinates(this.blockLocation);
		Location loc = Locator.parseLocation(this.blockLocation, null);
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
		cfg.set("block-type", this.blockType);
		cfg.set("click-type", click.name());
		cfg.set("location", this.blockLocation.isEmpty() ? null : this.blockLocation);
	}

	@Override
	public void load(ConfigurationSection cfg) {
		this.blockType = ItemUtil.getMaterial(cfg.getString("block-type", ""));
		click = ClickType.getByName(cfg.getString("click-type", "ANY"));
		this.blockLocation = cfg.getString("location", "");
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.BLOCK_CLICK;
	}

	enum ClickType {
		RIGHT,
		LEFT,
		ANY;

		public static ClickType getByName(String clickStr) {
			if (clickStr.equalsIgnoreCase("left")) return ClickType.LEFT;
			if (clickStr.equalsIgnoreCase("any")) return ClickType.ANY;
			return ClickType.RIGHT;
		}
	}

	private boolean clickCheck(boolean leftClick) {
		switch (click) {
			case ANY:
				return true;
			case LEFT:
				return leftClick;
			case RIGHT:
				return !leftClick;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
		if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
		if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
		if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
		sb.append(" (");
		sb.append("type:").append(blockType == null ? "-" : blockType);
		sb.append(" click:").append(this.click.name());
		sb.append(" loc:").append(blockLocation.isEmpty() ? "-" : blockLocation);
		sb.append(")");
		return sb.toString();
	}

	public boolean isValid() {
		// return (this.blockType == null || this.blockType.isEmpty()) && (this.blockLocation == null || this.blockLocation.isEmpty());
		return true;
	}

}
