package fun.reactions.module.worldedit.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.worldedit.external.RaWorldEdit;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 18/10/2017
 */
public class WeToolControlAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        boolean isToolControl = params.getBoolean("value", () -> params.getBoolean(Parameters.ORIGIN, false));
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        RaWorldEdit.getSession(player).setToolControl(isToolControl);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "WE_TOOLCONTROL";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
