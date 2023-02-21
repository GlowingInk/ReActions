package fun.reactions.module.basics.flags;

import fun.reactions.model.activity.flags.Flag;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 5/4/2017.
 */
@Aliased.Names("WALKSPEED")
public class WalkSpeedFlag implements Flag {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        Player player = env.getPlayer();
        if (!NumberUtils.isNumber(params, Is.NATURAL)) return false;
        long walkSpeed = Math.round(player.getWalkSpeed() * 10); // TODO: Why?
        env.getVariables().set("walkspeed", Long.toString(walkSpeed));
        return walkSpeed >= Integer.parseInt(params);
    }

    @Override
    public @NotNull String getName() {
        return "WALK_SPEED";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
