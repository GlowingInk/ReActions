package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-05-16.
 */
public class ActionWalkSpeed extends Action {
    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        double speed = params.getInteger("speed", params.getInteger("param-line", 0));
        if (params.contains("player"))
            player = Utils.getPlayerExact(params.getString("player"));
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
