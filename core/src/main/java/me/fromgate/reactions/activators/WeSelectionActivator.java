package me.fromgate.reactions.activators;

import me.fromgate.reactions.actions.Actions;
import me.fromgate.reactions.externals.worldedit.WeSelection;
import me.fromgate.reactions.storage.RAStorage;
import me.fromgate.reactions.storage.WeSelectionRegionStorage;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Variables;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

// TODO: Flag to check selection size of player
public class WeSelectionActivator extends Activator {
	private int maxBlocks;
	private int minBlocks;
	private String typeSelection;

	public WeSelectionActivator(String name, String param) {
		super(name, "activators");
	   Param params = new Param(param);
		minBlocks = params.getParam("minblocks", 0);
		maxBlocks = params.getParam("maxblocks", 0);
		typeSelection = params.getParam("type", "ANY");
	}

	public WeSelectionActivator(String name, String group, YamlConfiguration cfg) {
		super(name, group, cfg);
	}

	@Override
	public boolean activate(RAStorage event) {
		WeSelectionRegionStorage e = (WeSelectionRegionStorage) event;
		WeSelection selection = e.getSelection();
		if (!selection.isValid()) return false;

		int selectionBlocks = selection.getArea();
		Variables.setTempVar("selblocks", Integer.toString(selectionBlocks));
		if (selectionBlocks < minBlocks) return false;
		if (selectionBlocks > maxBlocks && maxBlocks != 0) return false;

		String selType = selection.getSelType();
		Variables.setTempVar("seltype", selType);
		if (!checkTypeSelection(selType)) return false;

		String region = selection.getRegion();
		if (region == null || region.isEmpty()) return false;

		World world = selection.getWorld();
		Variables.setTempVar("world", (world != null) ? world.getName() : "");

		Variables.setTempVar("region", region);
		return Actions.executeActivator(e.getPlayer(), this);
	}

	private boolean checkTypeSelection(String selType) {
		return typeSelection.isEmpty() || typeSelection.equalsIgnoreCase("ANY") || typeSelection.equalsIgnoreCase(selType);
	}

	@Override
	public void save(ConfigurationSection cfg) {
		cfg.set("min-blocks", this.minBlocks);
		cfg.set("max-blocks", this.maxBlocks);
		cfg.set("type", this.typeSelection);
	}

	@Override
	public void load(ConfigurationSection cfg) {
		this.minBlocks = cfg.getInt("min-blocks", 0);
		this.maxBlocks = cfg.getInt("max-blocks", 0);
		this.typeSelection = cfg.getString("type", "ANY");
	}

	@Override
	public ActivatorType getType() {
		return ActivatorType.WE_SELECTION_REGION;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append(" [").append(getType()).append("]");
		if (!getFlags().isEmpty()) sb.append(" F:").append(getFlags().size());
		if (!getActions().isEmpty()) sb.append(" A:").append(getActions().size());
		if (!getReactions().isEmpty()) sb.append(" R:").append(getReactions().size());
		sb.append(" (");
		sb.append("minblocks:").append(minBlocks);
		sb.append(" maxblocks:").append(maxBlocks);
		sb.append(" type:").append(typeSelection);
		sb.append(")");
		return sb.toString();
	}
}
