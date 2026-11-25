package fun.reactions.module.basic.actions;

import fun.reactions.menu.Menu;
import fun.reactions.menu.MenuManager;
import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import net.kyori.adventure.key.InvalidKeyException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"MENU_ITEM", "MENU"})
public class OpenMenuAction implements Action, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        MenuManager menus = env.getPlatform().getMenus();
        Menu menu;
        if (params.contains("menu")) {
            menu = menus.getMenu(params.getString("menu"));
            if (menu == null) return false;
            if (params.contains("close")) menu = menu.withCloseOnClick(params.getBoolean("close"));
        } else {
            try {
                menu = Menu.fromParameters(params);
            } catch (InvalidKeyException ex) {
                env.warn("Invalid menu type: " + ex.getMessage());
                return false;
            }
        }
        return menus.open(player, menu, env.getVariables());
    }

    @Override
    public @NotNull String getName() {
        return "OPEN_MENU";
    }
}
