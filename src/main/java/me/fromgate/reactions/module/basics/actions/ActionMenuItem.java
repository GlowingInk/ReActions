package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.menu.InventoryMenu;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Alias("MENU_ITEM")
public class ActionMenuItem extends Action {

    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        return InventoryMenu.createAndOpenInventory(context.getPlayer(), params, context.getVariables());
    }

    @Override
    public @NotNull String getName() {
        return "OPEN_MENU";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

}
