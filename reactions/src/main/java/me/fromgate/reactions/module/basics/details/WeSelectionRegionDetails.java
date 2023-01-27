package me.fromgate.reactions.module.basics.details;

import me.fromgate.reactions.externals.worldedit.WeSelection;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.Details;
import me.fromgate.reactions.logic.context.Variable;
import me.fromgate.reactions.module.basics.activators.WESelectionActivator;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static me.fromgate.reactions.logic.context.Variable.plain;
import static me.fromgate.reactions.logic.context.Variable.property;

public class WeSelectionRegionDetails extends Details {
    private final WeSelection selection;

    public WeSelectionRegionDetails(Player player, WeSelection weSelection) {
        super(player);
        this.selection = weSelection;
    }

    @Override
    public @NotNull Class<? extends Activator> getType() {
        return WESelectionActivator.class;
    }

    @Override
    protected @NotNull Map<String, Variable> prepareVariables() {
        Map<String, Variable> vars = new HashMap<>();
        vars.put(CANCEL_EVENT, property(false));
        if (selection.isValid()) {
            vars.put("seltype", plain(selection.selType()));
            World world = selection.world();
            vars.put("world", plain((world != null) ? world.getName() : ""));
            vars.put("selblocks", plain(selection.area()));
            vars.put("region", plain(selection.region()));
        }
        return vars;
    }

    public WeSelection getSelection() {return this.selection;}
}
