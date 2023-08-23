package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 05/04/2017
 */
public class FlySpeedFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        if (!NumberUtils.isNumber(paramsStr, Is.NATURAL)) return false;
        long flySpeed = Math.round(player.getFlySpeed() * 10);
        env.getVariables().set("flyspeed", Integer.toString((int) flySpeed));
        return flySpeed >= Integer.parseInt(paramsStr);
    }

    @Override
    public @NotNull String getName() {
        return "FLY_SPEED";
    }
}
