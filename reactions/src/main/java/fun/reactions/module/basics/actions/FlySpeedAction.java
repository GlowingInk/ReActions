package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 16/05/2017
 */
@Aliased.Names("FLIGHT_SPEED")
public class FlySpeedAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        double speed = params.getInteger("speed", () -> params.getInteger(Parameters.ORIGIN));
        if (params.contains("player")) {
            player = params.get("player", Bukkit::getPlayerExact);
        }
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
        if (speed > 1) {
            speed = 1;
        } else if (speed < 0) {
            speed = 0;
        }
        player.setFlySpeed((float) speed);
        return true;
    }
}