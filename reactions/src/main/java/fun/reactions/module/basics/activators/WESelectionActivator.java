package fun.reactions.module.basics.activators;

import fun.reactions.externals.worldedit.WeSelection;
import fun.reactions.logic.Logic;
import fun.reactions.logic.activators.ActivationContext;
import fun.reactions.logic.activators.Activator;
import fun.reactions.module.basics.details.WeSelectionRegionContext;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"WE_SELECTION_REGION", "WESELECTION"})
public class WESelectionActivator extends Activator {
    private final int maxBlocks;
    private final int minBlocks;
    private final String typeSelection;

    private WESelectionActivator(Logic base, int maxBlocks, int minBlocks, String typeSelection) {
        super(base);
        this.maxBlocks = maxBlocks;
        this.minBlocks = minBlocks;
        this.typeSelection = typeSelection;
    }

    public static WESelectionActivator create(Logic base, Parameters param) {
        int minBlocks = param.getInteger("minblocks");
        int maxBlocks = param.getInteger("maxblocks", Integer.MAX_VALUE);
        String typeSelection = param.getString("type", "ANY");
        return new WESelectionActivator(base, minBlocks, maxBlocks, typeSelection);
    }

    public static WESelectionActivator load(Logic base, ConfigurationSection cfg) {
        int minBlocks = cfg.getInt("min-blocks", 0);
        int maxBlocks = cfg.getInt("max-blocks", Integer.MAX_VALUE);
        String typeSelection = cfg.getString("type", "ANY");
        return new WESelectionActivator(base, minBlocks, maxBlocks, typeSelection);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        WeSelectionRegionContext e = (WeSelectionRegionContext) context;
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
    public void saveOptions(@NotNull ConfigurationSection cfg) {
        cfg.set("min-blocks", minBlocks);
        cfg.set("max-blocks", maxBlocks);
        cfg.set("type", typeSelection);
    }

    @Override
    public String toString() {
        String sb = super.toString() + " (" +
                "minblocks:" + minBlocks +
                "; maxblocks:" + maxBlocks +
                "; type:" + typeSelection +
                ")";
        return sb;
    }
}
