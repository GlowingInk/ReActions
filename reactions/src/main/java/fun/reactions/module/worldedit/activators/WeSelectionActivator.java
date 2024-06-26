package fun.reactions.module.worldedit.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.worldedit.external.WeSelection;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Aliased.Names({"WE_SELECTION_REGION", "WESELECTION"})
public class WeSelectionActivator extends Activator {
    private final int maxBlocks;
    private final int minBlocks;
    private final String typeSelection;

    private WeSelectionActivator(Logic base, int maxBlocks, int minBlocks, String typeSelection) {
        super(base);
        this.maxBlocks = maxBlocks;
        this.minBlocks = minBlocks;
        this.typeSelection = typeSelection;
    }

    public static WeSelectionActivator create(Logic base, Parameters param) {
        int minBlocks = param.getInteger("minblocks");
        int maxBlocks = param.getInteger("maxblocks", Integer.MAX_VALUE);
        String typeSelection = param.getString("type", "ANY");
        return new WeSelectionActivator(base, minBlocks, maxBlocks, typeSelection);
    }

    public static WeSelectionActivator load(Logic base, ConfigurationSection cfg) {
        int minBlocks = cfg.getInt("min-blocks", 0);
        int maxBlocks = cfg.getInt("max-blocks", Integer.MAX_VALUE);
        String typeSelection = cfg.getString("type", "ANY");
        return new WeSelectionActivator(base, minBlocks, maxBlocks, typeSelection);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        Context e = (Context) context;
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

    public static class Context extends ActivationContext {
        private final WeSelection selection;

        public Context(Player player, WeSelection weSelection) {
            super(player);
            this.selection = weSelection;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return WeSelectionActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            Map<String, Variable> vars = new HashMap<>();
            vars.put(CANCEL_EVENT, Variable.property(false));
            if (selection.isValid()) {
                vars.put("seltype", Variable.simple(selection.selType()));
                World world = selection.world();
                vars.put("world", Variable.simple((world != null) ? world.getName() : ""));
                vars.put("selblocks", Variable.simple(selection.area()));
                vars.put("region", Variable.simple(selection.region()));
            }
            return vars;
        }

        public WeSelection getSelection() {return this.selection;}
    }
}
