package fun.reactions.module.worldedit.contexts;

import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.module.worldedit.activators.WeSelectionActivator;
import fun.reactions.module.worldedit.external.WeSelection;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class WeSelectionContext extends ActivationContext {
    private final WeSelection selection;

    public WeSelectionContext(Player player, WeSelection weSelection) {
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
