package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/8/2017.
 */
public class ActionGlide implements Action {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        boolean isGlide = params.getBoolean("glide", true);
        return glidePlayer(player, isGlide);
    }

    @Override
    public @NotNull String getName() {
        return "GLIDE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private boolean glidePlayer(Player player, boolean isGlide) {
        if (player == null || player.isDead() || !player.isOnline()) return false;
        player.setGliding(isGlide);
        return true;
    }
}
