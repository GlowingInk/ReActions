package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 05/08/2017
 */
public class GlideAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = params.get("player", Bukkit::getPlayerExact, env.getPlayer()); // TODO Selectors
        if (player == null) return false;
        boolean isGlide = params.getBoolean("glide", true);
        return glidePlayer(player, isGlide);
    }

    @Override
    public @NotNull String getName() {
        return "GLIDE";
    }

    private boolean glidePlayer(Player player, boolean isGlide) {
        if (player == null || player.isDead() || !player.isOnline()) return false;
        player.setGliding(isGlide);
        return true;
    }
}
