package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class WalkSpeedAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        double speed = params.getInteger("speed", () -> params.getInteger(Parameters.ORIGIN));
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        return walkSpeedPlayer(player, speed / 10);
    }

    @Override
    public @NotNull String getName() {
        return "WALK_SPEED";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private boolean walkSpeedPlayer(Player player, double speed) {
        if (player == null || player.isDead() || !player.isOnline()) return false;
        if (speed > 1) speed = 1;
        if (speed < 0) speed = 0;
        player.setWalkSpeed((float) speed);
        return true;
    }
}
