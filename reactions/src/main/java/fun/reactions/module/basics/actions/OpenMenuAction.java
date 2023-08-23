package fun.reactions.module.basics.actions;

import fun.reactions.menu.InventoryMenu;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"MENU_ITEM", "MENU"})
public class OpenMenuAction implements Action, Activity.Personal {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        return InventoryMenu.createAndOpenInventory(env.getPlayer(), params, env.getVariables());
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        return InventoryMenu.createAndOpenInventory(player, params, env.getVariables());
    }

    @Override
    public @NotNull String getName() {
        return "OPEN_MENU";
    }
}
