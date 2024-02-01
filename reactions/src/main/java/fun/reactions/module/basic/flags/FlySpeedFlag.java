package fun.reactions.module.basic.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.num.Is;
import fun.reactions.util.num.NumberUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

/**
 * @author MaxDikiy
 * @since 05/04/2017
 */
public class FlySpeedFlag implements Flag, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        OptionalInt paramsOpt = NumberUtils.parseInteger(paramsStr, Is.NON_NEGATIVE);
        if (paramsOpt.isEmpty()) return false;
        long flySpeed = Math.round(player.getFlySpeed() * 10);
        env.getVariables().set("flyspeed", Integer.toString((int) flySpeed));
        return flySpeed >= paramsOpt.getAsInt();
    }

    @Override
    public @NotNull String getName() {
        return "FLY_SPEED";
    }
}
