package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class ActionFlySpeed implements Action {
    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        double speed = params.getInteger("speed", () -> params.getInteger("origin-string", 0));
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        return flySpeedPlayer(player, speed / 10);
    }

    @Override
    public @NotNull String getName() {
        return "FLY_SPEED";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private boolean flySpeedPlayer(Player player, double speed) {
        if (player == null || player.isDead() || !player.isOnline()) return false;
        if (speed > 1) speed = 1;
        if (speed < 0) speed = 0;
        player.setFlySpeed((float) speed);
        return true;
    }
}