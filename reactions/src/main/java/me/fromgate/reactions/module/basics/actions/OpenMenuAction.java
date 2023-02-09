package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.menu.InventoryMenu;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("MENU_ITEM")
public class OpenMenuAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
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
