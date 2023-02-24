package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/4/2017.
 */
public class FlySpeedFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        Player player = env.getPlayer();
        if (!NumberUtils.isNumber(content, Is.NATURAL)) return false;
        long flySpeed = Math.round(player.getFlySpeed() * 10);
        env.getVariables().set("flyspeed", Integer.toString((int) flySpeed));
        return flySpeed >= Integer.parseInt(content);
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
