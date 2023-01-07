package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.NumberUtils;
import me.fromgate.reactions.util.NumberUtils.Is;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/4/2017.
 */
public class FlagFlySpeed implements Flag {
    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String params) {
        Player player = context.getPlayer();
        if (!NumberUtils.isNumber(params, Is.NATURAL)) return false;
        long flySpeed = Math.round(player.getFlySpeed() * 10);
        context.setVariable("flyspeed", Integer.toString((int) flySpeed));
        return flySpeed >= Integer.parseInt(params);
    }

    @Override
    public @NotNull String getName() {
        return "FLY_SPEED";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
