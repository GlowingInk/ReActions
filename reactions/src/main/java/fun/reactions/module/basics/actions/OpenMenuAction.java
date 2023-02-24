package fun.reactions.module.basics.actions;

import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("MENU_ITEM")
public class OpenMenuAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Parameters params = Parameters.fromString(content);
        return InventoryMenu.createAndOpenInventory(env.getPlayer(), params, env.getVariables());
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
