package me.fromgate.reactions.module.basics.activators;

import me.fromgate.reactions.externals.worldedit.WeSelection;
import me.fromgate.reactions.logic.ActivatorLogic;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Storage;
import me.fromgate.reactions.module.basics.storages.WeSelectionRegionStorage;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;

public class WeSelectionActivator extends Activator {
    private final int maxBlocks;
    private final int minBlocks;
    private final String typeSelection;

    private WeSelectionActivator(ActivatorLogic base, int maxBlocks, int minBlocks, String typeSelection) {
        super(base);
        this.maxBlocks = maxBlocks;
        this.minBlocks = minBlocks;
        this.typeSelection = typeSelection;
    }

    public static WeSelectionActivator create(ActivatorLogic base, Parameters param) {
        int minBlocks = param.getInteger("minblocks", 0);
        int maxBlocks = param.getInteger("maxblocks", Integer.MAX_VALUE);
        String typeSelection = param.getString("type", "ANY");
        return new WeSelectionActivator(base, minBlocks, maxBlocks, typeSelection);
    }

    public static WeSelectionActivator load(ActivatorLogic base, ConfigurationSection cfg) {
        int minBlocks = cfg.getInt("min-blocks", 0);
        int maxBlocks = cfg.getInt("max-blocks", Integer.MAX_VALUE);
        String typeSelection = cfg.getString("type", "ANY");
        return new WeSelectionActivator(base, minBlocks, maxBlocks, typeSelection);
    }

    @Override
    public boolean checkStorage(Storage event) {
        WeSelectionRegionStorage e = (WeSelectionRegionStorage) event;
        WeSelection selection = e.getSelection();
        if (!selection.isValid()) return false;
        int selectionBlocks = selection.area();
        if (selectionBlocks < minBlocks) return false;
        if (selectionBlocks > maxBlocks && maxBlocks != 0) return false;
        String selType = selection.selType();
        if (!checkTypeSelection(selType)) return false;
        String region = selection.region();
        return region != null && !region.isEmpty();
    }

    private boolean checkTypeSelection(String selType) {
        return typeSelection.isEmpty() || typeSelection.equalsIgnoreCase("ANY") || typeSelection.equalsIgnoreCase(selType);
    }

    @Override
    public void saveOptions(ConfigurationSection cfg) {
        cfg.set("min-blocks", minBlocks);
        cfg.set("max-blocks", maxBlocks);
        cfg.set("type", typeSelection);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" (");
        sb.append("minblocks:").append(minBlocks);
        sb.append("; maxblocks:").append(maxBlocks);
        sb.append("; type:").append(typeSelection);
        sb.append(")");
        return sb.toString();
    }
}
